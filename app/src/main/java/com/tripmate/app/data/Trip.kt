package com.tripmate.app.data

import com.google.firebase.Timestamp

data class Trip(
    val id: String = "",
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val memo: String = "",
    val ownerId: String = "",
    val inviteCode: String = "",
    val memberIds: List<String> = emptyList(),
    val isDomestic: Boolean = true,
    val baseCurrency: String = "KRW",
    val exchangeRateToKrw: Double? = null,
    val mapProvider: String = "naver",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val dateRangeText: String
        get() = if (startDate.isNotBlank() && endDate.isNotBlank()) {
            "$startDate ~ $endDate"
        } else {
            "여행 기간 미정"
        }
}
