package com.jeju.evtravel.ui.map

import com.jeju.evtravel.domain.model.Place

sealed interface MapUiState {
    object Idle : MapUiState
    object Loading : MapUiState
    data class Success(val places: List<Place>) : MapUiState
    data class Error(val message: String) : MapUiState
}