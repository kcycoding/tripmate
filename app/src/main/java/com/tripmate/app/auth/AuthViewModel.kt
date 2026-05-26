package com.tripmate.app.auth

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { repository.saveCurrentUserIfSignedIn() }
                .onSuccess { user ->
                    _uiState.value = AuthUiState(isLoading = false, user = user)
                }
                .onFailure { throwable ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        user = repository.currentUserProfile(),
                        errorMessage = throwable.message ?: "사용자 정보를 저장하지 못했습니다."
                    )
                }
        }
    }

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.signInWithGoogle(activity) }
                .onSuccess { user ->
                    _uiState.value = AuthUiState(isLoading = false, user = user)
                }
                .onFailure { throwable ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        user = repository.currentUserProfile(),
                        errorMessage = throwable.message ?: "Google 로그인에 실패했습니다."
                    )
                }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState(isLoading = false)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
