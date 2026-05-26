package com.tripmate.app.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripmate.app.data.CreateTripInput
import com.tripmate.app.data.TripRepository
import com.tripmate.app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateTripViewModel(
    private val repository: TripRepository = TripRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

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

    fun createTrip(owner: UserProfile) {
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
                repository.createTrip(
                    CreateTripInput(
                        title = state.title,
                        destination = state.destination,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        memo = state.memo,
                        isDomestic = state.isDomestic,
                        baseCurrency = state.baseCurrency.ifBlank { "KRW" },
                        exchangeRateToKrw = exchangeRate,
                        mapProvider = state.mapProvider,
                        owner = owner
                    )
                )
            }.onSuccess {
                _uiState.value = CreateTripUiState(isSaved = true)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "여행을 저장하지 못했습니다."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSaved() {
        _uiState.value = CreateTripUiState()
    }

    private fun update(reducer: CreateTripUiState.() -> CreateTripUiState) {
        _uiState.update { it.reducer().copy(errorMessage = null) }
    }

    private fun validate(state: CreateTripUiState): String? {
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
