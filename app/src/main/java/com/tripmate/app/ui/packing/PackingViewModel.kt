package com.tripmate.app.ui.packing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.tripmate.app.data.PackingItem
import com.tripmate.app.data.PackingRepository
import com.tripmate.app.data.Trip
import com.tripmate.app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PackingViewModel(
    private val repository: PackingRepository = PackingRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackingUiState())
    val uiState: StateFlow<PackingUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var observedTripId: String? = null

    fun observeTrip(trip: Trip) {
        if (observedTripId == trip.id && listenerRegistration != null) return

        listenerRegistration?.remove()
        observedTripId = trip.id
        _uiState.value = PackingUiState(isLoading = true)
        listenerRegistration = repository.observePackingItems(trip.id) { result ->
            result
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "준비물을 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun updateNewItemTitle(value: String) {
        _uiState.update { it.copy(newItemTitle = value.take(40), errorMessage = null) }
    }

    fun add(trip: Trip, user: UserProfile) {
        val title = _uiState.value.newItemTitle.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "준비물 이름을 입력해 주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.addPackingItem(trip.id, title, user.id) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, newItemTitle = "") }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "준비물을 추가하지 못했습니다."
                        )
                    }
                }
        }
    }

    fun setChecked(tripId: String, item: PackingItem, isChecked: Boolean, userId: String) {
        viewModelScope.launch {
            runCatching { repository.setChecked(tripId, item.id, isChecked, userId) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "체크 상태를 변경하지 못했습니다.") }
                }
        }
    }

    fun delete(tripId: String, itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deletePackingItem(tripId, itemId) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "준비물을 삭제하지 못했습니다.") }
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
