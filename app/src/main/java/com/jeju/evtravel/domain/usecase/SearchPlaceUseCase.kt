package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository

/**
 * 장소 검색을 담당하는 유스케이스 클래스
 *
 * @property repository 장소 데이터를 가져오기 위한 리포지토리
 */
class SearchPlaceUseCase(
    private val repository: PlaceRepository
) {
    /**
     * 주어진 검색어와 위치 정보로 장소 검색
     *
     * @param query 검색어
     * @param x 현재 경도 (longitude)
     * @param y 현재 위도 (latitude)
     */
    suspend operator fun invoke(
        query: String,
        x: Double,
        y: Double,
        radius: Int? = 2000 // 기본 2km 반경
    ): List<Place> {
        return repository.searchNearbyPlaces(
            query = query,
            x = x,
            y = y,
            radius = radius
        )
    }
}