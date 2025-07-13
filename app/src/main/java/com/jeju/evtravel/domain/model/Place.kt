package com.jeju.evtravel.domain.model

/**
 * 도메인 레이어에서 사용되는 장소 정보를 나타내는 데이터 클래스
 * @property id 장소의 고유 식별자
 * @property name 장소의 이름
 * @property address 장소의 주소
 * @property type 장소의 유형 (예: 카페, 음식점 등)
 */
data class Place(
    val id: String,
    val name: String,
    val address: String,
    val type: String
)