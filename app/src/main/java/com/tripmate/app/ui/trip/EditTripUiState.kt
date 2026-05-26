package com.tripmate.app.ui.trip

data class EditTripUiState(
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val memo: String = "",
    val isDomestic: Boolean = true,
    val baseCurrency: String = "KRW",
    val exchangeRateToKrw: String = "",
    val mapProvider: String = "naver",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)
