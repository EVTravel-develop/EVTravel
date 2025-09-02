package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.domain.model.TourPlace

interface TourPlaceRepository {
    suspend fun getNearbyTourPlaces(
        mapX: Double,
        mapY: Double,
        radius: Int? = 2000,
        pageNo: Int? = 1,
        numOfRows: Int? = 15,
        arrange: String? = "E",
        type: String? = "json",
        contentTypeId: String? = "",
        mobileOs: String? = "AND",
        mobileApp: String? = "EVTravel",
        areaCode: String? = "",
        sigunguCode: String? = "",
        cat1: String? = "",
        cat2: String? = "",
        cat3: String? = "",
        lDongRegnCd: String? = "",
        lDongSignguCd: String? = "",
        lclsSystm1: String? = "",
        lclsSystm2: String? = "",
        lclsSystm3: String? = "",
    ): List<TourPlace>
}