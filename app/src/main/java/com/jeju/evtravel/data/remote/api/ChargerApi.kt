package com.jeju.evtravel.data.remote.api
import com.jeju.evtravel.data.remote.dto.ChargerApiResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ChargerApi {
    @GET("/B552584/EvCharger/getChargerInfo")
    suspend fun getChargerInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("dataType") dataType: String = "JSON",
        @Query("zcode") zcode: String,
        @Query("zscode") zscode: String
    ): ChargerApiResponseDto

    // row 응답 확인용 로그 api
    @GET("/B552584/EvCharger/getChargerInfo")
    suspend fun getChargerInfoRaw(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("dataType") dataType: String = "JSON",
        @Query("zcode") zcode: String,
        @Query("zscode") zscode: String
    ): Response<ResponseBody>
}