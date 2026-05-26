package com.tripmate.app.data

import com.google.firebase.Timestamp

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
