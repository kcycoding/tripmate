package com.tripmate.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.tripmate.app.data.ScheduleItem
import com.tripmate.app.data.ScheduleItemInput
import com.tripmate.app.data.ScheduleRepository
import com.tripmate.app.data.Trip
import com.tripmate.app.data.UserProfile
import com.tripmate.app.data.scheduleCategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: ScheduleRepository = ScheduleRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var observedTripId: String? = null

    fun observeTrip(trip: Trip) {
        if (observedTripId == trip.id && listenerRegistration != null) return

        listenerRegistration?.remove()
        observedTripId = trip.id
        _uiState.value = ScheduleUiState(
            isLoading = true,
            form = ScheduleFormState(date = trip.startDate, category = scheduleCategories.first())
        )
        listenerRegistration = repository.observeScheduleItems(trip.id) { result ->
            result
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "일정을 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun startCreate(defaultDate: String) {
        _uiState.update {
            it.copy(
                form = ScheduleFormState(date = defaultDate, category = scheduleCategories.first()),
                isSaved = false,
                errorMessage = null
            )
        }
    }

    fun updateDate(value: String) = updateForm { copy(date = value) }
    fun updateTime(value: String) = updateForm { copy(time = value) }
    fun updatePlaceName(value: String) = updateForm { copy(placeName = value) }
    fun updateTitle(value: String) = updateForm { copy(title = value) }
    fun updateMemo(value: String) = updateForm { copy(memo = value) }
    fun updateExpectedCost(value: String) = updateForm { copy(expectedCost = value.filter { it.isDigit() || it == '.' }.take(12)) }
    fun updateCategory(value: String) = updateForm { copy(category = value) }
    fun updateTravelTimeMemo(value: String) = updateForm { copy(travelTimeMemo = value) }

    fun startEdit(item: ScheduleItem) {
        _uiState.update {
            it.copy(
                form = ScheduleFormState(
                    editingItemId = item.id,
                    date = item.date,
                    time = item.time,
                    placeName = item.placeName,
                    title = item.title,
                    memo = item.memo,
                    expectedCost = item.expectedCost?.let { cost -> if (cost % 1.0 == 0.0) cost.toLong().toString() else cost.toString() }.orEmpty(),
                    category = item.category,
                    travelTimeMemo = item.travelTimeMemo
                ),
                isSaved = false,
                errorMessage = null
            )
        }
    }

    fun cancelEdit(defaultDate: String) {
        _uiState.update { it.copy(form = ScheduleFormState(date = defaultDate), errorMessage = null) }
    }

    fun save(trip: Trip, user: UserProfile) {
        val state = _uiState.value
        val form = state.form
        val validationMessage = validate(form, trip)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        val input = ScheduleItemInput(
            date = form.date,
            time = form.time,
            placeName = form.placeName,
            title = form.title,
            memo = form.memo,
            expectedCost = form.expectedCost.toDoubleOrNull(),
            category = form.category,
            travelTimeMemo = form.travelTimeMemo,
            userId = user.id
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, isSaved = false, errorMessage = null) }
            runCatching {
                if (form.editingItemId == null) {
                    val nextSortOrder = (state.items.filter { it.date == form.date }.maxOfOrNull { it.sortOrder } ?: 0L) + 1000L
                    repository.addScheduleItem(trip.id, input, nextSortOrder)
                } else {
                    repository.updateScheduleItem(trip.id, form.editingItemId, input)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        form = ScheduleFormState(date = form.date, category = form.category)
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "일정을 저장하지 못했습니다."
                    )
                }
            }
        }
    }

    fun delete(tripId: String, itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteScheduleItem(tripId, itemId) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "일정을 삭제하지 못했습니다.") }
                }
        }
    }

    fun move(tripId: String, item: ScheduleItem, direction: Int, userId: String) {
        val sameDayItems = _uiState.value.items.filter { it.date == item.date }.sortedWith(compareBy<ScheduleItem> { it.sortOrder }.thenBy { it.time })
        val currentIndex = sameDayItems.indexOfFirst { it.id == item.id }
        val target = sameDayItems.getOrNull(currentIndex + direction) ?: return
        viewModelScope.launch {
            runCatching { repository.swapSortOrder(tripId, item, target, userId) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "일정 순서를 변경하지 못했습니다.") }
                }
        }
    }

    fun resetSaved() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateForm(reducer: ScheduleFormState.() -> ScheduleFormState) {
        _uiState.update { it.copy(form = it.form.reducer(), errorMessage = null) }
    }

    private fun validate(form: ScheduleFormState, trip: Trip): String? {
        val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")
        val timeRegex = Regex("\\d{2}:\\d{2}")
        return when {
            !dateRegex.matches(form.date) -> "날짜는 YYYY-MM-DD 형식으로 입력해 주세요."
            form.date < trip.startDate || form.date > trip.endDate -> "여행 기간 안의 날짜를 입력해 주세요."
            form.time.isNotBlank() && !timeRegex.matches(form.time) -> "시간은 HH:MM 형식으로 입력해 주세요."
            form.title.isBlank() -> "일정 제목을 입력해 주세요."
            form.placeName.isBlank() -> "장소명을 입력해 주세요."
            form.expectedCost.isNotBlank() && form.expectedCost.toDoubleOrNull() == null -> "예상 비용은 숫자로 입력해 주세요."
            form.category !in scheduleCategories -> "카테고리를 선택해 주세요."
            else -> null
        }
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        listenerRegistration = null
        super.onCleared()
    }
}


