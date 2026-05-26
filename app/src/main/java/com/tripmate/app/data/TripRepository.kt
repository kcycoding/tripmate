package com.tripmate.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class TripRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeTripsForUser(
        userId: String,
        onResult: (Result<List<Trip>>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("trips")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val trips = snapshot?.documents.orEmpty().map { document ->
                    document.toTrip()
                }.sortedWith(compareBy<Trip> { it.startDate }.thenBy { it.title })

                onResult(Result.success(trips))
            }
    }

    suspend fun createTrip(input: CreateTripInput): String {
        val tripRef = firestore.collection("trips").document()
        val now = Timestamp.now()
        val inviteCode = generateInviteCode()
        val tripData = mapOf(
            "id" to tripRef.id,
            "title" to input.title.trim(),
            "destination" to input.destination.trim(),
            "startDate" to input.startDate.trim(),
            "endDate" to input.endDate.trim(),
            "memo" to input.memo.trim(),
            "ownerId" to input.owner.id,
            "inviteCode" to inviteCode,
            "memberIds" to listOf(input.owner.id),
            "isDomestic" to input.isDomestic,
            "baseCurrency" to input.baseCurrency.trim().uppercase(),
            "exchangeRateToKrw" to input.exchangeRateToKrw,
            "mapProvider" to input.mapProvider,
            "createdAt" to now,
            "updatedAt" to now
        )
        val memberData = mapOf(
            "userId" to input.owner.id,
            "role" to "owner",
            "displayName" to input.owner.displayName,
            "email" to input.owner.email,
            "photoUrl" to input.owner.photoUrl,
            "joinedAt" to now
        )
        val inviteData = mapOf(
            "code" to inviteCode,
            "tripId" to tripRef.id,
            "ownerId" to input.owner.id,
            "createdAt" to now
        )

        firestore.batch()
            .set(tripRef, tripData)
            .set(tripRef.collection("members").document(input.owner.id), memberData)
            .set(firestore.collection("inviteCodes").document(inviteCode), inviteData)
            .commit()
            .await()

        return tripRef.id
    }

    suspend fun joinTripByInviteCode(inviteCode: String, user: UserProfile): String {
        val code = inviteCode.trim().uppercase()
        require(code.isNotBlank()) { "초대 코드를 입력해 주세요." }

        val inviteSnapshot = firestore.collection("inviteCodes").document(code).get().await()
        if (!inviteSnapshot.exists()) {
            throw IllegalArgumentException("초대 코드를 찾을 수 없습니다.")
        }

        val tripId = inviteSnapshot.getString("tripId").orEmpty()
        if (tripId.isBlank()) {
            throw IllegalStateException("초대 코드 정보가 올바르지 않습니다.")
        }

        val now = Timestamp.now()
        val tripRef = firestore.collection("trips").document(tripId)
        val memberRef = tripRef.collection("members").document(user.id)
        val memberData = mapOf(
            "userId" to user.id,
            "role" to "member",
            "displayName" to user.displayName,
            "email" to user.email,
            "photoUrl" to user.photoUrl,
            "joinedAt" to now
        )

        firestore.batch()
            .update(
                tripRef,
                mapOf(
                    "memberIds" to FieldValue.arrayUnion(user.id),
                    "updatedAt" to now
                )
            )
            .set(memberRef, memberData)
            .commit()
            .await()

        return tripId
    }


    suspend fun updateTrip(tripId: String, input: EditTripInput) {
        firestore.collection("trips").document(tripId)
            .update(
                mapOf(
                    "title" to input.title.trim(),
                    "destination" to input.destination.trim(),
                    "startDate" to input.startDate.trim(),
                    "endDate" to input.endDate.trim(),
                    "memo" to input.memo.trim(),
                    "isDomestic" to input.isDomestic,
                    "baseCurrency" to input.baseCurrency.trim().uppercase(),
                    "exchangeRateToKrw" to input.exchangeRateToKrw,
                    "mapProvider" to input.mapProvider,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
    }

    suspend fun deleteTrip(tripId: String, inviteCode: String) {
        val tripRef = firestore.collection("trips").document(tripId)
        val scheduleItems = tripRef.collection("scheduleItems").get().await().documents
        val packingItems = tripRef.collection("packingItems").get().await().documents
        val members = tripRef.collection("members").get().await().documents
        val batch = firestore.batch()

        scheduleItems.forEach { batch.delete(it.reference) }
        packingItems.forEach { batch.delete(it.reference) }
        members.forEach { batch.delete(it.reference) }
        if (inviteCode.isNotBlank()) {
            batch.delete(firestore.collection("inviteCodes").document(inviteCode))
        }
        batch.delete(tripRef)
        batch.commit().await()
    }
    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()
        return buildString {
            repeat(6) {
                append(chars[random.nextInt(chars.length)])
            }
        }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toTrip(): Trip {
    return Trip(
        id = getString("id").orEmpty().ifBlank { id },
        title = getString("title").orEmpty(),
        destination = getString("destination").orEmpty(),
        startDate = getString("startDate").orEmpty(),
        endDate = getString("endDate").orEmpty(),
        memo = getString("memo").orEmpty(),
        ownerId = getString("ownerId").orEmpty(),
        inviteCode = getString("inviteCode").orEmpty(),
        memberIds = (get("memberIds") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        isDomestic = getBoolean("isDomestic") ?: true,
        baseCurrency = getString("baseCurrency").orEmpty().ifBlank { "KRW" },
        exchangeRateToKrw = getDouble("exchangeRateToKrw"),
        mapProvider = getString("mapProvider").orEmpty().ifBlank { "naver" },
        createdAt = getTimestamp("createdAt"),
        updatedAt = getTimestamp("updatedAt")
    )
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}


