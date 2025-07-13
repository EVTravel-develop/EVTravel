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
     * 주어진 검색어로 장소를 검색
     *
     * @param query 검색어 (대소문자 구분 없이 검색됨)
     * @return 검색어가 이름에 포함된 장소 리스트 (대소문자 구분 없음)
     */
    suspend operator fun invoke(query: String): List<Place> {
        // 저장소에서 모든 장소를 가져온 후, 이름에 검색어가 포함된 장소만 필터링
        return repository.getPlaces().filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}
