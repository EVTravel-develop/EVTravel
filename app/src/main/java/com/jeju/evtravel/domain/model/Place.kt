package com.jeju.evtravel.domain.model

/**
 * Place는 장소 정보를 나타내는 데이터 클래스입니다.
 * 카카오 로컬 API에서 제공하는 장소 정보를 매핑합니다.
 *
 * @property id 장소의 고유 ID
 * @property name 장소 이름
 * @property address 주소 (일반 주소)
 * @property roadAddress 도로명 주소
 * @property longitude 경도
 * @property latitude 위도
 * @property url 장소 URL
 * @property distanceMeters 검색 결과와의 거리 (미터 단위)
 */
data class Place(
    val id: String, // id
    val name: String, // place_name
    val address: String?, // address_name
    val roadAddress: String?, // road_address_name
    val longitude: Double, // x
    val latitude: Double, // y
    val url: String?, // place_url
    val distanceMeters: Int?, // distance

    var chargerList: List<ChargerInfo> = emptyList()
)