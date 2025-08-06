package com.jeju.evtravel.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlaceSearchResponseDto(
    @SerializedName("documents") val documents: List<PlaceDto>,
    @SerializedName("meta") val meta: MetaDto
)

data class PlaceDto(
    @SerializedName("id") val id: String,
    @SerializedName("place_name") val placeName: String,
    //@SerializedName("category_name") val categoryName: String?,
    @SerializedName("address_name") val addressName: String?,
    @SerializedName("road_address_name") val roadAddressName: String?,
    @SerializedName("x") val x: String, // longitude
    @SerializedName("y") val y: String, // latitude
    @SerializedName("place_url") val placeUrl: String?,
    @SerializedName("distance") val distance: String?,
)

data class MetaDto(
    @SerializedName("is_end") val isEnd: Boolean,
    @SerializedName("pageable_count") val pageableCount: Int,
    @SerializedName("total_count") val totalCount: Int
)