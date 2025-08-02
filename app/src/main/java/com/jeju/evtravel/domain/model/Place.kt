package com.jeju.evtravel.domain.model

data class Place(
    val id: String, // id
    val name: String, // place_name
    //val category: String?,
    //category_group_code	String
    //category_group_name	String
    //phone	String
    val address: String?, // address_name
    val roadAddress: String?, // road_address_name
    val longitude: Double, // x
    val latitude: Double, // y
    val url: String?, // place_url
    val distanceMeters: Int? // distance
)
