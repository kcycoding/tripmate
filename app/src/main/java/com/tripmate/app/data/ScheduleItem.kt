package com.tripmate.app.data

import com.google.firebase.Timestamp

val scheduleCategories = listOf("관광", "식사", "이동", "숙소", "쇼핑", "기타")

data class ScheduleItem(
    val id: String = "",
    val tripId: String = "",
    val date: String = "",
    val time: String = "",
    val placeName: String = "",
    val title: String = "",
    val memo: String = "",
    val expectedCost: Double? = null,
    val category: String = "기타",
    val travelTimeMemo: String = "",
    val sortOrder: Long = 0L,
    val createdBy: String = "",
    val updatedBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val costText: String
        get() = expectedCost?.let { "%,.0f".format(it) } ?: "-"
}

data class ScheduleItemInput(
    val date: String,
    val time: String,
    val placeName: String,
    val title: String,
    val memo: String,
    val expectedCost: Double?,
    val category: String,
    val travelTimeMemo: String,
    val userId: String
)
