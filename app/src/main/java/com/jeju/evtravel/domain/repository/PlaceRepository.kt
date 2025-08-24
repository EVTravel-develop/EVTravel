package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.domain.model.Place

/**
 * PlaceRepository는 장소 검색 관련 기능을 제공하는 인터페이스입니다.
 * 카카오 로컬 API를 통해 주변 장소를 검색하는 메서드를 정의합니다.
 */
interface PlaceRepository {
    /**
     * 지정된 좌표 주변의 장소를 검색합니다.
     *
     * @param query 검색어 (예: 음식점, 카페 등)
     * @param x 경도 (longitude)
     * @param y 위도 (latitude)
     * @param radius 검색 반경 (미터 단위, 기본값은 null로 전체 범위 검색)
     * @param page 페이지 번호 (기본값은 1)
     * @param size 한 페이지당 결과 개수 (기본값은 15)
     * @param sort 정렬 기준 (기본값은 거리 기준 "distance")
     * @return 검색된 장소 목록
     */
    suspend fun searchNearbyPlaces(
        query: String,
        x: Double?,
        y: Double?,
        radius: Int? = null,
        page: Int? = 1,
        size: Int? = 15
    ): List<Place>
}