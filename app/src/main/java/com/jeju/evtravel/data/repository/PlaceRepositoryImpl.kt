package com.jeju.evtravel.data.repository


import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.data.remote.mapper.toDomain
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository

class PlaceRepositoryImpl(
    private val api: KakaoLocalApi,
    private val restApiKey: String
) : PlaceRepository {

    override suspend fun searchNearbyPlaces(
        query: String,
        x: Double,
        y: Double,
        radius: Int?,
        page: Int,
        size: Int,
        sort: String
    ): List<Place> {
        val dto = api.searchKeyword(
            authorization = "KakaoAK $restApiKey",
            query = query,
            x = x,
            y = y,
            radius = radius,
            page = page,
            size = size,
            sort = sort
        )
        return dto.documents.map { it.toDomain() }
    }
}