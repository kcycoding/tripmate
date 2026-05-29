package com.tripmate.app.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeExpenseItems(
        tripId: String,
        onResult: (Result<List<ExpenseItem>>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("trips")
            .document(tripId)
            .collection("expenseItems")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val items = snapshot?.documents.orEmpty().map { document ->
                    ExpenseItem(
                        id = document.getString("id").orEmpty().ifBlank { document.id },
                        tripId = document.getString("tripId").orEmpty(),
                        scheduleItemId = document.getString("scheduleItemId"),
                        date = document.getString("date").orEmpty(),
                        title = document.getString("title").orEmpty(),
                        category = document.getString("category").orEmpty().ifBlank { "기타" },
                        amount = document.getDouble("amount") ?: 0.0,
                        memo = document.getString("memo").orEmpty(),
                        createdBy = document.getString("createdBy").orEmpty(),
                        updatedBy = document.getString("updatedBy").orEmpty(),
                        createdAt = document.getTimestamp("createdAt"),
                        updatedAt = document.getTimestamp("updatedAt")
                    )
                }.sortedWith(compareBy<ExpenseItem> { it.date }.thenBy { it.createdAt?.seconds ?: 0L })

                onResult(Result.success(items))
            }
    }

    suspend fun addExpenseItem(tripId: String, input: ExpenseItemInput): String {
        val itemRef = firestore.collection("trips").document(tripId).collection("expenseItems").document()
        val now = Timestamp.now()
        itemRef.set(
            mapOf(
                "id" to itemRef.id,
                "tripId" to tripId,
                "scheduleItemId" to input.scheduleItemId,
                "date" to input.date,
                "title" to input.title.trim(),
                "category" to input.category,
                "amount" to input.amount,
                "memo" to input.memo.trim(),
                "createdBy" to input.userId,
                "updatedBy" to input.userId,
                "createdAt" to now,
                "updatedAt" to now
            )
        ).await()
        return itemRef.id
    }

    suspend fun updateExpenseItem(tripId: String, itemId: String, input: ExpenseItemInput) {
        firestore.collection("trips").document(tripId).collection("expenseItems").document(itemId)
            .update(
                mapOf(
                    "scheduleItemId" to input.scheduleItemId,
                    "date" to input.date,
                    "title" to input.title.trim(),
                    "category" to input.category,
                    "amount" to input.amount,
                    "memo" to input.memo.trim(),
                    "updatedBy" to input.userId,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
    }

    suspend fun deleteExpenseItem(tripId: String, itemId: String) {
        firestore.collection("trips").document(tripId).collection("expenseItems").document(itemId)
            .delete()
            .await()
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
