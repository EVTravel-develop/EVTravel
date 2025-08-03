package com.jeju.evtravel.data.model

/**
 * 장소 정보를 담는 데이터 클래스 (카카오 API 기준)
 * @property id 장소의 고유 식별자 (카카오 API place_id)
 * @property name 장소의 이름 (place_name)
 * @property categoryGroupCode 카테고리 코드 (예: FD6=음식점, CE7=카페 등)
 * @property roadAddressName 도로명 주소 (road_address_name)
 * @property x 경도 (longitude)
 * @property y 위도 (latitude)
 */
data class PlaceDto(
    val id: String = "",
    val name: String = "",
    val categoryGroupCode: String = "",
    val roadAddressName: String = "",
    val x: Double = 0.0,  // 경도
    val y: Double = 0.0   // 위도
)