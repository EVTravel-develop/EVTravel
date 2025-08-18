package com.jeju.evtravel.ui.planner

import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.model.Charger

/**
 * UI에서 사용되는 장소 정보를 나타내는 데이터 클래스
 * Place 모델을 확장하여 UI 관련 속성을 추가
 *
 * @property place 장소 정보 (Place 모델)
 * @property isExpanded 장소가 확장되었는지 여부
 * @property chargers 해당 장소의 충전소 목록 (선택적)
 */
data class UiPlace(
    val place: Place,
    val isExpanded: Boolean = false,
    val chargers: List<Charger>? = null
)