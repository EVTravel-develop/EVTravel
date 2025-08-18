package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.domain.model.Charger

interface ChargerRepository {
    suspend fun getNearbyChargers(
        lat: Double,
        lon: Double
    ): List<Charger>
}