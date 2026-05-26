package com.tripmate.app.data

data class CreateTripInput(
    val title: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val memo: String,
    val isDomestic: Boolean,
    val baseCurrency: String,
    val exchangeRateToKrw: Double?,
    val mapProvider: String,
    val owner: UserProfile
)
