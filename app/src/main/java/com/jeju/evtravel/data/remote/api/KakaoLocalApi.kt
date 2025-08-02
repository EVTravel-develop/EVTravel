package com.jeju.evtravel.data.remote.api

import com.jeju.evtravel.data.remote.dto.PlaceSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApi {

    @GET("/v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") authorization: String, // "KakaoAK {REST_API_KEY}"
        @Query("query") query: String,
        @Query("x") x: Double,  // longitude
        @Query("y") y: Double,  // latitude
        @Query("radius") radius: Int? = null, // 0~20000
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15,
        @Query("sort") sort: String = "distance"   // distance | accuracy
    ): PlaceSearchResponseDto
}