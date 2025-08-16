package com.jeju.evtravel.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KakaoRegionResponse(
    @SerializedName("documents") val documents: List<KakaoRegionDoc> = emptyList()
)

data class KakaoRegionDoc(
    @SerializedName("region_type") val regionType: String, // "B" or "H"
    @SerializedName("address_name") val addressName: String,
    @SerializedName("region_1depth_name") val region1: String, // 시/도: 서울특별시, 경기도...
    @SerializedName("region_2depth_name") val region2: String, // 시/군/구
    @SerializedName("region_3depth_name") val region3: String, // 읍/면/동
    @SerializedName("region_4depth_name") val region4: String, // 리
    @SerializedName("code") val code: String, // 10자리 법정동 코드, 예) 4113510900
    @SerializedName("x") val x: Double,
    @SerializedName("y") val y: Double
)