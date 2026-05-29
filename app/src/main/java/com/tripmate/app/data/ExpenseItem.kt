package com.tripmate.app.data

import com.google.firebase.Timestamp

data class ExpenseItem(
    val id: String = "",
    val tripId: String = "",
    val scheduleItemId: String? = null,
    val date: String = "",
    val title: String = "",
    val category: String = "기타",
    val amount: Double = 0.0,
    val memo: String = "",
    val createdBy: String = "",
    val updatedBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val amountText: String
        get() = "%,.0f".format(amount)
}

data class ExpenseItemInput(
    val scheduleItemId: String?,
    val date: String,
    val title: String,
    val category: String,
    val amount: Double,
    val memo: String,
    val userId: String
)
