package com.tripmate.app.ui.trip

data class CreateTripUiState(
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
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
