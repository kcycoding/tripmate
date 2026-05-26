package com.tripmate.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TripRepositoryFirestoreTest {
    @Test
    fun signedInUserCanCreateAndDeleteTrip() = runBlocking {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        assertNotNull("Google login must be completed on the emulator before this test.", firebaseUser)

        val user = firebaseUser ?: return@runBlocking
        val owner = UserProfile(
            id = user.uid,
            displayName = user.displayName.orEmpty().ifBlank { "Test User" },
            email = user.email.orEmpty(),
            photoUrl = user.photoUrl?.toString().orEmpty()
        )
        val repository = TripRepository()
        val firestore = FirebaseFirestore.getInstance()

        val tripId = repository.createTrip(
            CreateTripInput(
                title = "Delete Flow Test",
                destination = "Osaka",
                startDate = "2026-06-10",
                endDate = "2026-06-13",
                memo = "Created and deleted by connected Android test.",
                isDomestic = true,
                baseCurrency = "KRW",
                exchangeRateToKrw = null,
                mapProvider = "naver",
                owner = owner
            )
        )
        assertNotNull(tripId)

        val tripSnapshot = firestore.collection("trips").document(tripId).get().await()
        val inviteCode = tripSnapshot.getString("inviteCode").orEmpty()
        assertNotNull(inviteCode)

        PackingRepository().addPackingItem(tripId, "Test passport", owner.id)
        ScheduleRepository().addScheduleItem(
            tripId = tripId,
            input = ScheduleItemInput(
                date = "2026-06-10",
                time = "09:00",
                placeName = "Osaka Station",
                title = "Test schedule",
                memo = "cleanup check",
                expectedCost = 1000.0,
                category = "관광",
                travelTimeMemo = "10분",
                userId = owner.id
            ),
            nextSortOrder = 1000L
        )

        repository.deleteTrip(tripId, inviteCode)

        val inviteSnapshot = firestore.collection("inviteCodes").document(inviteCode).get().await()
        assertFalse("Invite code should be deleted with the trip.", inviteSnapshot.exists())
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
