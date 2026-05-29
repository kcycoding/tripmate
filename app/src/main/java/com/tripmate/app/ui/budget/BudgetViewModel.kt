package com.tripmate.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.tripmate.app.data.ExpenseItem
import com.tripmate.app.data.ExpenseItemInput
import com.tripmate.app.data.ExpenseRepository
import com.tripmate.app.data.ScheduleItem
import com.tripmate.app.data.Trip
import com.tripmate.app.data.UserProfile
import com.tripmate.app.data.scheduleCategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var observedTripId: String? = null

    fun observeTrip(trip: Trip) {
        if (observedTripId == trip.id && listenerRegistration != null) return

        listenerRegistration?.remove()
        observedTripId = trip.id
        _uiState.value = BudgetUiState(isLoading = true, form = BudgetFormState(date = trip.startDate))
        listenerRegistration = repository.observeExpenseItems(trip.id) { result ->
            result
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "예산을 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun startCreate(defaultDate: String) {
        _uiState.update {
            it.copy(
                form = BudgetFormState(date = defaultDate, category = scheduleCategories.first()),
                errorMessage = null
            )
        }
    }

    fun startEdit(item: ExpenseItem) {
        _uiState.update {
            it.copy(
                form = BudgetFormState(
                    editingItemId = item.id,
                    scheduleItemId = item.scheduleItemId,
                    date = item.date,
                    title = item.title,
                    category = item.category,
                    amount = if (item.amount % 1.0 == 0.0) item.amount.toLong().toString() else item.amount.toString(),
                    memo = item.memo
                ),
                errorMessage = null
            )
        }
    }

    fun selectScheduleItem(item: ScheduleItem?) {
        _uiState.update { state ->
            state.copy(
                form = if (item == null) {
                    state.form.copy(scheduleItemId = null, title = "", memo = "")
                } else {
                    state.form.copy(
                        scheduleItemId = item.id,
                        date = item.date,
                        title = item.title,
                        category = item.category,
                        memo = state.form.memo.ifBlank { item.placeName }
                    )
                },
                errorMessage = null
            )
        }
    }

    fun updateDate(value: String) = updateForm { copy(date = value) }
    fun updateTitle(value: String) = updateForm { copy(title = value.take(60), scheduleItemId = scheduleItemId) }
    fun updateCategory(value: String) = updateForm { copy(category = value) }
    fun updateAmount(value: String) = updateForm { copy(amount = value.filter { it.isDigit() || it == '.' }.take(12)) }
    fun updateMemo(value: String) = updateForm { copy(memo = value.take(300)) }

    fun save(trip: Trip, user: UserProfile) {
        val form = _uiState.value.form
        val validationMessage = validate(form, trip)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        val input = ExpenseItemInput(
            scheduleItemId = form.scheduleItemId,
            date = form.date,
            title = form.title,
            category = form.category,
            amount = form.amount.toDouble(),
            memo = form.memo,
            userId = user.id
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                if (form.editingItemId == null) {
                    repository.addExpenseItem(trip.id, input)
                } else {
                    repository.updateExpenseItem(trip.id, form.editingItemId, input)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        form = BudgetFormState(date = form.date, category = form.category)
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "예산을 저장하지 못했습니다."
                    )
                }
            }
        }
    }

    fun delete(tripId: String, itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteExpenseItem(tripId, itemId) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "예산을 삭제하지 못했습니다.") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateForm(reducer: BudgetFormState.() -> BudgetFormState) {
        _uiState.update { it.copy(form = it.form.reducer(), errorMessage = null) }
    }

    private fun validate(form: BudgetFormState, trip: Trip): String? {
        val dateRegex = Regex("\\d{4}-\\d{2}-\\d{2}")
        return when {
            !dateRegex.matches(form.date) -> "날짜는 YYYY-MM-DD 형식으로 입력해 주세요."
            form.date < trip.startDate || form.date > trip.endDate -> "여행 기간 안의 날짜를 입력해 주세요."
            form.title.isBlank() -> "예산 항목 이름을 입력해 주세요."
            form.amount.toDoubleOrNull() == null || form.amount.toDouble() <= 0.0 -> "실제 사용 금액을 입력해 주세요."
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
