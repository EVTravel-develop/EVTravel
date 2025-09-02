package com.jeju.evtravel.domain.model

data class TourPlace(
    val title: String?,
    val address: String?,
    val x: Double,
    val y: Double,
    val dist: Double?,
    val contentId: String,
    val contentTypeId: String,
    val firstImage: String?,
    val firstImage2: String?,
    val areaCode: String?,
    val sigunguCode: String?,
    val tel: String?,
    val cat1: String?,
    val cat2: String?,
    val cat3: String?,
    val lclsSystm1: String?,
    val lclsSystm2: String?,
    val lclsSystm3: String?,
    val zipCode: String?,
)
