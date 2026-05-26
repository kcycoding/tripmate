package com.tripmate.app.auth

import com.tripmate.app.data.UserProfile

data class AuthUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val errorMessage: String? = null
) {
    val isSignedIn: Boolean = user != null
}
