package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.data.remote.mapper.toDomain
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository

/**
 * PlaceRepositoryImpl은 PlaceRepository 인터페이스를 구현하여
 * 카카오 로컬 API를 통해 장소 검색 기능을 제공합니다.
 *
 * @property api 카카오 로컬 API 인스턴스
 * @property restApiKey 카카오 REST API 키
 */
class PlaceRepositoryImpl(
    private val api: KakaoLocalApi,
    private val restApiKey: String
) : PlaceRepository {
    /**
     * 지정된 좌표 주변의 장소를 검색합니다.
     */
    override suspend fun searchNearbyPlaces(
        query: String,
        x: Double?,
        y: Double?,
        radius: Int?,
        page: Int?,
        size: Int?
    ): List<Place> {
        val dto = api.searchKeyword(
            authorization = "KakaoAK $restApiKey",
            query = query,
            x = x,
            y = y,
            radius = radius,
            page = page,
            size = size
        )
        return dto.documents.map { it.toDomain() }
    }
}