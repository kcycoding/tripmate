package com.tripmate.app.ui.packing

import com.tripmate.app.data.PackingItem

data class PackingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val newItemTitle: String = "",
    val items: List<PackingItem> = emptyList(),
    val errorMessage: String? = null
)
