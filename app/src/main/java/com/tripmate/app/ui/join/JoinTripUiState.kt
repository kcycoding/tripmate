package com.tripmate.app.ui.join

data class JoinTripUiState(
    val inviteCode: String = "",
    val isJoining: Boolean = false,
    val isJoined: Boolean = false,
    val joinedTripId: String? = null,
    val errorMessage: String? = null
)
