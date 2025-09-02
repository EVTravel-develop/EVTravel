package com.jeju.evtravel.data.remote.api

import com.jeju.evtravel.data.remote.dto.TourPlaceDetailResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TourPlaceApi {
    @GET("B551011/KorService2/detailCommon2")
    suspend fun getTourPlaceDetail(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int? = 1,
        @Query("numOfRows") numOfRows: Int? = 15,
        @Query("MobileOS") os: String = "AND",
        @Query("MobileApp") app: String = "EVTravel",
        @Query("_type") type: String = "json",
        @Query("contentId") contentId: String,
    ): TourPlaceDetailResponseDto
}