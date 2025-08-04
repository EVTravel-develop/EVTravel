package com.jeju.evtravel.domain.model

/**
 * Charger는 충전소 정보를 나타내는 데이터 클래스입니다.
 * 충전소 이름과 고속/완속 충전기 개수를 포함합니다.
 *
 * @property name 충전소 이름
 */
data class Charger(
    val name: String, // 충전소 이름
    // val fastCount: Int,
    // val slowCount: Int
)