package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.domain.model.TourPlaceDetail

interface TourPlaceDetailRepository {
    suspend fun getTourPlaceContentId(
        contentId: String,
        pageNo: Int? = 1,
        numOfRows: Int? = 15,
        os: String = "AND",
        app: String = "EVTravel",
        type: String = "json"
    ): TourPlaceDetail
}