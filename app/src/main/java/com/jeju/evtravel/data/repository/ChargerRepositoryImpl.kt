package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.domain.model.Charger
import com.jeju.evtravel.domain.repository.ChargerRepository

class ChargerRepositoryImpl(
    private val api: KakaoLocalApi,
    private val restApiKey: String
) : ChargerRepository {

    override suspend fun getNearbyChargers(lat: Double, lon: Double): List<Charger> {
        val response = api.searchChargers(
            authorization = "KakaoAK $restApiKey",
            x = lon,
            y = lat
        )

        return response.documents.map {
            Charger(
                name = it.placeName
            )
        }
    }
}