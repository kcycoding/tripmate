package com.tripmate.app.ui.budget

import com.tripmate.app.data.ExpenseItem
import com.tripmate.app.data.scheduleCategories

data class BudgetUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val items: List<ExpenseItem> = emptyList(),
    val form: BudgetFormState = BudgetFormState(),
    val errorMessage: String? = null
)

data class BudgetFormState(
    val editingItemId: String? = null,
    val scheduleItemId: String? = null,
    val date: String = "",
    val title: String = "",
    val category: String = scheduleCategories.first(),
    val amount: String = "",
    val memo: String = ""
) {
    val isEditing: Boolean
        get() = editingItemId != null
}
