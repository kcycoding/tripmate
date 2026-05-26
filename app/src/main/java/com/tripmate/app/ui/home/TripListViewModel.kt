package com.tripmate.app.ui.home

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.tripmate.app.data.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TripListViewModel(
    private val repository: TripRepository = TripRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripListUiState())
    val uiState: StateFlow<TripListUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var observedUserId: String? = null

    fun observeTrips(userId: String) {
        if (observedUserId == userId && listenerRegistration != null) return

        listenerRegistration?.remove()
        observedUserId = userId
        _uiState.value = TripListUiState(isLoading = true)
        listenerRegistration = repository.observeTripsForUser(userId) { result ->
            result
                .onSuccess { trips ->
                    _uiState.value = TripListUiState(isLoading = false, trips = trips)
                }
                .onFailure { throwable ->
                    _uiState.value = TripListUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "여행 목록을 불러오지 못했습니다."
                    )
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        listenerRegistration = null
        super.onCleared()
    }
}
