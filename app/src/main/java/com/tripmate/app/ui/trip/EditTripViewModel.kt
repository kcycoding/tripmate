package com.tripmate.app.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripmate.app.data.EditTripInput
import com.tripmate.app.data.Trip
import com.tripmate.app.data.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditTripViewModel(
    private val repository: TripRepository = TripRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditTripUiState())
    val uiState: StateFlow<EditTripUiState> = _uiState.asStateFlow()

    private var editingTripId: String? = null
    private var editingInviteCode: String = ""

    fun load(trip: Trip) {
        if (editingTripId == trip.id) return
        editingTripId = trip.id
        editingInviteCode = trip.inviteCode
        _uiState.value = EditTripUiState(
            title = trip.title,
            destination = trip.destination,
            startDate = trip.startDate,
            endDate = trip.endDate,
            memo = trip.memo,
            isDomestic = trip.isDomestic,
            baseCurrency = trip.baseCurrency,
            exchangeRateToKrw = trip.exchangeRateToKrw?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty(),
            mapProvider = trip.mapProvider
        )
    }

    fun reset() {
        editingTripId = null
        editingInviteCode = ""
        _uiState.value = EditTripUiState()
    }

    fun updateTitle(value: String) = update { copy(title = value) }
    fun updateDestination(value: String) = update { copy(destination = value) }
    fun updateStartDate(value: String) = update { copy(startDate = value) }
    fun updateEndDate(value: String) = update { copy(endDate = value) }
    fun updateMemo(value: String) = update { copy(memo = value) }
    fun updateDomestic(value: Boolean) = update {
        copy(
            isDomestic = value,
            baseCurrency = if (value) "KRW" else baseCurrency,
            exchangeRateToKrw = if (value) "" else exchangeRateToKrw
        )
    }
    fun updateBaseCurrency(value: String) = update { copy(baseCurrency = value.uppercase().take(3)) }
    fun updateExchangeRate(value: String) = update { copy(exchangeRateToKrw = value) }
    fun updateMapProvider(value: String) = update { copy(mapProvider = value) }

    fun save() {
        val tripId = editingTripId ?: return
        val state = _uiState.value
        val validationMessage = validate(state)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val exchangeRate = if (state.isDomestic) null else state.exchangeRateToKrw.toDoubleOrNull()
            runCatching {
                repository.updateTrip(
                    tripId,
                    EditTripInput(
                        title = state.title,
                        destination = state.destination,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        memo = state.memo,
                        isDomestic = state.isDomestic,
                        baseCurrency = state.baseCurrency.ifBlank { "KRW" },
                        exchangeRateToKrw = exchangeRate,
                        mapProvider = state.mapProvider
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = throwable.message ?: "여행을 수정하지 못했습니다.")
                }
            }
        }
    }

    fun delete() {
        val tripId = editingTripId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            runCatching { repository.deleteTrip(tripId, editingInviteCode) }
                .onSuccess { _uiState.update { it.copy(isDeleting = false, isDeleted = true) } }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isDeleting = false, errorMessage = throwable.message ?: "여행을 삭제하지 못했습니다.")
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun update(reducer: EditTripUiState.() -> EditTripUiState) {
        _uiState.update { it.reducer().copy(errorMessage = null, isSaved = false, isDeleted = false) }
    }

    private fun validate(state: EditTripUiState): String? {
        val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")
        return when {
            state.title.isBlank() -> "여행 제목을 입력해 주세요."
            state.destination.isBlank() -> "여행지를 입력해 주세요."
            !dateRegex.matches(state.startDate) -> "시작일은 YYYY-MM-DD 형식으로 입력해 주세요."
            !dateRegex.matches(state.endDate) -> "종료일은 YYYY-MM-DD 형식으로 입력해 주세요."
            state.endDate < state.startDate -> "종료일은 시작일보다 빠를 수 없습니다."
            !state.isDomestic && state.baseCurrency.isBlank() -> "해외 여행 통화를 입력해 주세요."
            !state.isDomestic && state.exchangeRateToKrw.toDoubleOrNull() == null -> "원화 환산 환율을 숫자로 입력해 주세요."
            else -> null
        }
    }
}

