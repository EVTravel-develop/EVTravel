package com.jeju.evtravel.data.remote.api

import com.jeju.evtravel.data.remote.dto.PlaceSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApi {

    @GET("/v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") authorization: String, // "KakaoAK {REST_API_KEY}"
        @Query("query") query: String,
        @Query("x") x: Double?,  // longitude
        @Query("y") y: Double?,  // latitude
        @Query("radius") radius: Int? = null, // 0~20000
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PlaceSearchResponseDto>

    @GET("/v2/local/search/keyword.json")
    suspend fun searchChargers(
        @Header("Authorization") authorization: String,
        @Query("query") query: String = "전기차 충전소",
        @Query("x") x: Double?,
        @Query("y") y: Double?,
        @Query("radius") radius: Int? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PlaceSearchResponseDto>
}