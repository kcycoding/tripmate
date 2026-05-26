package com.tripmate.app.ui.schedule

import com.tripmate.app.data.ScheduleItem
import com.tripmate.app.data.scheduleCategories

data class ScheduleFormState(
    val editingItemId: String? = null,
    val date: String = "",
    val time: String = "",
    val placeName: String = "",
    val title: String = "",
    val memo: String = "",
    val expectedCost: String = "",
    val category: String = scheduleCategories.first(),
    val travelTimeMemo: String = ""
) {
    val isEditing: Boolean
        get() = editingItemId != null
}

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val items: List<ScheduleItem> = emptyList(),
    val form: ScheduleFormState = ScheduleFormState(),
    val errorMessage: String? = null
)
