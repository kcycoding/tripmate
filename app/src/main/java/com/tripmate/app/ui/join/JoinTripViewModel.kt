package com.tripmate.app.ui.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripmate.app.data.TripRepository
import com.tripmate.app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinTripViewModel(
    private val repository: TripRepository = TripRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinTripUiState())
    val uiState: StateFlow<JoinTripUiState> = _uiState.asStateFlow()

    fun updateInviteCode(value: String) {
        _uiState.update {
            it.copy(
                inviteCode = value.uppercase().filter { char -> char.isLetterOrDigit() }.take(8),
                errorMessage = null,
                isJoined = false,
                joinedTripId = null
            )
        }
    }

    fun join(user: UserProfile) {
        val code = _uiState.value.inviteCode.trim().uppercase()
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "초대 코드를 입력해 주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }
            runCatching { repository.joinTripByInviteCode(code, user) }
                .onSuccess { tripId ->
                    _uiState.value = JoinTripUiState(
                        isJoining = false,
                        isJoined = true,
                        joinedTripId = tripId
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            errorMessage = throwable.message ?: "여행에 참여하지 못했습니다."
                        )
                    }
                }
        }
    }

    fun reset() {
        _uiState.value = JoinTripUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
