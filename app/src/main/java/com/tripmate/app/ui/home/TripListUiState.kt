package com.tripmate.app.ui.home

import com.tripmate.app.data.Trip

data class TripListUiState(
    val isLoading: Boolean = true,
    val trips: List<Trip> = emptyList(),
    val errorMessage: String? = null
)
