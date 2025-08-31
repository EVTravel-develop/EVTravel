package com.jeju.evtravel.data.remote.dto

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class TourPlaceDetailResponseDto(
    @SerializedName("response") val response: DetailResponse
)
data class DetailResponse(
    @SerializedName("header") val header: DetailHeader,
    @SerializedName("body") val body: DetailBody
)

data class DetailHeader(
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg") val resultMsg: String
)

data class DetailBody(
    @SerializedName("items") val items: DetailItems,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int
)

//@JsonAdapter(ItemsAdapter::class)
data class DetailItems(
    @SerializedName("item") val item: List<TourInfoItem>
)

data class TourInfoItem(
    @SerializedName("contentid") val contentId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("addr1") val addr1: String?,
    @SerializedName("addr2") val addr2: String?,
    @SerializedName("tel") val tel: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("firstimage") val firstImage: String?,
    @SerializedName("firstimage2") val firstImage2: String?,
    @SerializedName("mapx") val mapX: Double?,
    @SerializedName("mapy") val mapY: Double?
)