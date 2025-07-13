package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.domain.model.Place

/**
 * 장소 데이터에 접근하기 위한 저장소 인터페이스
 * 데이터 소스(로컬, 원격 등)로부터 장소 데이터를 가져오는 역할을 정의
 */
interface PlaceRepository {
    /**
     * 모든 장소 목록을 비동기적으로 가져옵니다.
     * @return 장소 도메인 모델의 리스트
     */
    suspend fun getPlaces(): List<Place>
}