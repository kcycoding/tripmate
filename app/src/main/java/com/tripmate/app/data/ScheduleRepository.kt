package com.tripmate.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ScheduleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeScheduleItems(
        tripId: String,
        onResult: (Result<List<ScheduleItem>>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("trips")
            .document(tripId)
            .collection("scheduleItems")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val items = snapshot?.documents.orEmpty().map { document ->
                    ScheduleItem(
                        id = document.getString("id").orEmpty().ifBlank { document.id },
                        tripId = document.getString("tripId").orEmpty(),
                        date = document.getString("date").orEmpty(),
                        time = document.getString("time").orEmpty(),
                        placeName = document.getString("placeName").orEmpty(),
                        title = document.getString("title").orEmpty(),
                        memo = document.getString("memo").orEmpty(),
                        expectedCost = document.getDouble("expectedCost"),
                        category = document.getString("category").orEmpty().ifBlank { "기타" },
                        travelTimeMemo = document.getString("travelTimeMemo").orEmpty(),
                        sortOrder = document.getLong("sortOrder") ?: 0L,
                        createdBy = document.getString("createdBy").orEmpty(),
                        updatedBy = document.getString("updatedBy").orEmpty(),
                        createdAt = document.getTimestamp("createdAt"),
                        updatedAt = document.getTimestamp("updatedAt")
                    )
                }.sortedWith(compareBy<ScheduleItem> { it.date }.thenBy { it.sortOrder }.thenBy { it.time })

                onResult(Result.success(items))
            }
    }

    suspend fun addScheduleItem(tripId: String, input: ScheduleItemInput, nextSortOrder: Long): String {
        val itemRef = firestore.collection("trips").document(tripId).collection("scheduleItems").document()
        val now = Timestamp.now()
        itemRef.set(
            mapOf(
                "id" to itemRef.id,
                "tripId" to tripId,
                "date" to input.date,
                "time" to input.time.trim(),
                "placeName" to input.placeName.trim(),
                "title" to input.title.trim(),
                "memo" to input.memo.trim(),
                "expectedCost" to input.expectedCost,
                "category" to input.category,
                "travelTimeMemo" to input.travelTimeMemo.trim(),
                "sortOrder" to nextSortOrder,
                "createdBy" to input.userId,
                "updatedBy" to input.userId,
                "createdAt" to now,
                "updatedAt" to now
            )
        ).await()
        return itemRef.id
    }

    suspend fun updateScheduleItem(tripId: String, itemId: String, input: ScheduleItemInput) {
        firestore.collection("trips").document(tripId).collection("scheduleItems").document(itemId)
            .update(
                mapOf(
                    "date" to input.date,
                    "time" to input.time.trim(),
                    "placeName" to input.placeName.trim(),
                    "title" to input.title.trim(),
                    "memo" to input.memo.trim(),
                    "expectedCost" to input.expectedCost,
                    "category" to input.category,
                    "travelTimeMemo" to input.travelTimeMemo.trim(),
                    "updatedBy" to input.userId,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
    }

    suspend fun deleteScheduleItem(tripId: String, itemId: String) {
        firestore.collection("trips").document(tripId).collection("scheduleItems").document(itemId)
            .delete()
            .await()
    }

    suspend fun swapSortOrder(tripId: String, first: ScheduleItem, second: ScheduleItem, userId: String) {
        val tripRef = firestore.collection("trips").document(tripId)
        val now = Timestamp.now()
        firestore.batch()
            .update(
                tripRef.collection("scheduleItems").document(first.id),
                mapOf("sortOrder" to second.sortOrder, "updatedBy" to userId, "updatedAt" to now)
            )
            .update(
                tripRef.collection("scheduleItems").document(second.id),
                mapOf("sortOrder" to first.sortOrder, "updatedBy" to userId, "updatedAt" to now)
            )
            .commit()
            .await()
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
