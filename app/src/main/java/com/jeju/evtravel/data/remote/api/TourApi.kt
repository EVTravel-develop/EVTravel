package com.jeju.evtravel.data.remote.api

import com.jeju.evtravel.data.remote.dto.PlaceTourResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TourApi {
    @GET("B551011/KorService2/locationBasedList2") // 앞에 슬래시 없어도 OK (baseUrl이 DATA)
    suspend fun getTourListInfo(
        @Query("serviceKey") serviceKey: String, // 키는 "원본 문자열"로 두고 Retrofit이 인코딩 하게 두는게 안전
        @Query("mapX") mapX: Double,
        @Query("mapY") mapY: Double,
        @Query("radius") radius: Int? = 500,
        @Query("pageNo") pageNo: Int? = 1,
        @Query("numOfRows") numOfRows: Int? = 15,
        @Query("arrange") arrange: String? = "E",
        @Query("_type") type: String = "json",   // non-null
        @Query("contentTypeId") contentTypeId: String? = "",
        @Query("MobileOS") mobileOs: String = "AND",
        @Query("MobileApp") mobileApp: String = "EVTravel",
        @Query("areaCode") areaCode: String? = "",
        @Query("sigunguCode") sigunguCode: String? = "",
        @Query("cat1") cat1: String? = "",
        @Query("cat2") cat2: String? = "",
        @Query("cat3") cat3: String? = "",
        @Query("lDongRegnCd") lDongRegnCd: String? = "",
        @Query("lDongSignguCd") lDongSignguCd: String? = "",
        @Query("lclsSystm1") lclsSystm1: String? = "",
        @Query("lclsSystm2") lclsSystm2: String? = "",
        @Query("lclsSystm3") lclsSystm3: String? = "",
    ): PlaceTourResponseDto
}
