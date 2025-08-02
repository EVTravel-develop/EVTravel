package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.domain.model.Place

interface PlaceRepository {
    suspend fun searchNearbyPlaces(
        query: String,
        x: Double,
        y: Double,
        radius: Int? = null,
        page: Int = 1,
        size: Int = 15,
        sort: String = "distance"
    ): List<Place>
}