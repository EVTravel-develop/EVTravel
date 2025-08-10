package com.jeju.evtravel.domain.model

/**
 * Charger는 충전소 정보를 나타내는 데이터 클래스입니다.
 * 충전소 이름, 주소, 위치 정보를 포함합니다.
 */
data class Charger(
    val name: String,              // 충전소 이름
    val address: String,           // 충전소 주소
    val latitude: Double,          // 위도
    val longitude: Double          // 경도
    // 추후 fastCount, slowCount 추가 가능
)