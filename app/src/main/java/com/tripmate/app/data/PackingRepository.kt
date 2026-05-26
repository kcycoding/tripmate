package com.tripmate.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class PackingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observePackingItems(
        tripId: String,
        onResult: (Result<List<PackingItem>>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("trips")
            .document(tripId)
            .collection("packingItems")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val items = snapshot?.documents.orEmpty().map { document ->
                    PackingItem(
                        id = document.getString("id").orEmpty().ifBlank { document.id },
                        tripId = document.getString("tripId").orEmpty(),
                        title = document.getString("title").orEmpty(),
                        isChecked = document.getBoolean("isChecked") ?: false,
                        createdBy = document.getString("createdBy").orEmpty(),
                        checkedBy = document.getString("checkedBy").orEmpty(),
                        createdAt = document.getTimestamp("createdAt"),
                        updatedAt = document.getTimestamp("updatedAt")
                    )
                }.sortedWith(compareBy<PackingItem> { it.isChecked }.thenBy { it.title })

                onResult(Result.success(items))
            }
    }

    suspend fun addPackingItem(tripId: String, title: String, userId: String): String {
        val itemRef = firestore.collection("trips").document(tripId).collection("packingItems").document()
        val now = Timestamp.now()
        itemRef.set(
            mapOf(
                "id" to itemRef.id,
                "tripId" to tripId,
                "title" to title.trim(),
                "isChecked" to false,
                "createdBy" to userId,
                "checkedBy" to "",
                "createdAt" to now,
                "updatedAt" to now
            )
        ).await()
        return itemRef.id
    }

    suspend fun setChecked(tripId: String, itemId: String, isChecked: Boolean, userId: String) {
        firestore.collection("trips").document(tripId).collection("packingItems").document(itemId)
            .update(
                mapOf(
                    "isChecked" to isChecked,
                    "checkedBy" to if (isChecked) userId else "",
                    "updatedAt" to Timestamp.now()
                )
            ).await()
    }

    suspend fun deletePackingItem(tripId: String, itemId: String) {
        firestore.collection("trips").document(tripId).collection("packingItems").document(itemId)
            .delete()
            .await()
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
