package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository

class SearchNearbyPlacesUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(
        query: String,
        x: Double?,
        y: Double?,
        radius: Int? = 2000, // 2000m 기본
        page: Int? = 1,
        size: Int? = 15
    ): List<Place> = repository.searchNearbyPlaces(query, x, y, radius, page, size)
}