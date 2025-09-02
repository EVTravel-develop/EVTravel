package com.jeju.evtravel.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class PlaceTourResponseDto(
    @SerializedName("response") val response: Response
)
data class Response(
    @SerializedName("header") val header: Header,
    @SerializedName("body") val body: Body
)

data class Header(
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg") val resultMsg: String
)

data class Body(
    @SerializedName("items") val items: Items,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int
)

data class Items(
    @SerializedName("item") val item: List<TourItem>
)

data class TourItem(
    @SerializedName("title") val title: String,
    @SerializedName("addr1") val addr1: String?,
    @SerializedName("mapx") val mapx: Double, // 경도
    @SerializedName("mapy") val mapy: Double, // 위도
    @SerializedName("dist") val dist: Double, // 현재 좌표와의 거리(m)
    @SerializedName("zipcode") val zipcode: String? = null, // 우편번호
    @SerializedName("contentid") val contentid: String,
    @SerializedName("contenttypeid") val contenttypeid: String,
    @SerializedName("firstimage") val firstimage: String? = null,   // 대표 이미지
    @SerializedName("firstimage2") val firstimage2: String? = null,  // 대체 이미지
    @SerializedName("areacode") val areacode: String?,  // 시도 코드
    @SerializedName("sigungucode") val sigungucode: String?,   // 시군구 코드
    @SerializedName("tel") val tel: String? = null,
    @SerializedName("lDongRegnCd") val lDongRegnCd: String,   // 법정동 시도 코드
    @SerializedName("lDongSignguCd") val lDongSignguCd: String, // 법정동 시군구 코드
    @SerializedName("cat1") val cat1: String?, // 카테고리 대분류
    @SerializedName("cat2") val cat2: String?, // 카테고리 중분류
    @SerializedName("cat3") val cat3: String?, // 카테고리 소분류
    @SerializedName("lclsSystm1") val lclsSystm1: String?, // 분류체계 대분류
    @SerializedName("lclsSystm2") val lclsSystm2: String?, // 분류체계 중분류
    @SerializedName("lclsSystm3") val lclsSystm3: String?, // 분류체계 소분류
)