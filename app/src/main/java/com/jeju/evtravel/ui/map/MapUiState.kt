package com.jeju.evtravel.ui.map

import com.jeju.evtravel.domain.model.Place

/**
 * 지도 UI의 상태를 나타내는 인터페이스
 * 콜백 메소드나 속성에 사용되며, 지도 상태가 변경될 때마다 콜백을 수행한다.
 *
 * @see MapViewModel
 * @see MapScreen
 */
sealed interface MapUiState {
    /** 초기 상태. 아무런 작업 없음 */
    object Idle : MapUiState

    /** 로딩 상태. 주변 검색 중 또는 다른 비동기 작업 진행 중 */
    object Loading : MapUiState

    /**
     * 성공 상태. 주변 검색 결과가 갱신되었을 때 발생
     *
     * @property places 검색 결과로 반환된 장소 목록
     */
    data class Success(val places: List<Place>) : MapUiState

    /**
     * 에러 상태. 주변 검색 또는 다른 비동기 작업 중 오류가 발생했을 때 발생
     *
     * @property message 오류 메시지
     */
    data class Error(val message: String) : MapUiState
}