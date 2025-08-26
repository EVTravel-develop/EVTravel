package com.jeju.evtravel.data.remote.api

import com.jeju.evtravel.data.remote.dto.KakaoRegionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalRegionApi {

    @GET("v2/local/geo/coord2regioncode.json")
    suspend fun getRegionCode(
        @Header("Authorization") authorization: String,
        @Query("x") x: Double,
        @Query("y") y: Double
    ): KakaoRegionResponse
}