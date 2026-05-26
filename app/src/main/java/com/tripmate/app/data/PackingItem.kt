package com.tripmate.app.data

import com.google.firebase.Timestamp

data class PackingItem(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val isChecked: Boolean = false,
    val createdBy: String = "",
    val checkedBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
