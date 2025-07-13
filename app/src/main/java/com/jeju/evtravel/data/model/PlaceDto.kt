package com.jeju.evtravel.data.model

/**
 * 장소 정보를 담는 데이터 클래스
 * @property id 장소의 고유 식별자
 * @property name 장소의 이름
 * @property address 장소의 주소
 * @property type 장소의 유형 (예: 카페, 음식점 등)
 */
data class PlaceDto(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val type: String = ""
)