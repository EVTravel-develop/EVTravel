package com.jeju.evtravel.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
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

@JsonAdapter(ItemsAdapter::class)
data class Items(
    @SerializedName("item") val item: List<TourItem>
)

data class TourItem(
    @SerializedName("title") val title: String,
    @SerializedName("addr1") val addr1: String?,
    @SerializedName("mapx") val mapx: Double, // 경도
    @SerializedName("mapy") val mapy: Double, // 위도
    @SerializedName("dist") val dist: Double, // 현재 좌표와의 거리(m)
    @SerializedName("zipcode") val zipcode: String?, // 우편번호
    @SerializedName("contentid") val contentid: String,
    @SerializedName("contenttypeid") val contenttypeid: String,
    @SerializedName("firstimage") val firstimage: String?,   // 대표 이미지
    @SerializedName("firstimage2") val firstimage2: String?,  // 대체 이미지
    @SerializedName("areacode") val areacode: String?,  // 시도 코드
    @SerializedName("sigungucode") val sigungucode: String?,   // 시군구 코드
    @SerializedName("tel") val tel: String?,
    @SerializedName("lDongRegnCd") val lDongRegnCd: String,   // 법정동 시도 코드
    @SerializedName("lDongSignguCd") val lDongSignguCd: String, // 법정동 시군구 코드
    @SerializedName("cat1") val cat1: String?, // 카테고리 대분류
    @SerializedName("cat2") val cat2: String?, // 카테고리 중분류
    @SerializedName("cat3") val cat3: String?, // 카테고리 소분류
    @SerializedName("lclsSystm1") val lclsSystm1: String?, // 분류체계 대분류
    @SerializedName("lclsSystm2") val lclsSystm2: String?, // 분류체계 중분류
    @SerializedName("lclsSystm3") val lclsSystm3: String?, // 분류체계 소분류
)

class ItemsAdapter : JsonDeserializer<Items> {
    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext
    ): Items {
        if (json == null || json.isJsonNull) return Items(emptyList())

        // 정상 케이스: items가 객체 { "item": [...] } 인 경우
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            val itemEl = obj.get("item")
            return when {
                itemEl == null || itemEl.isJsonNull -> Items(emptyList())
                itemEl.isJsonArray -> {
                    val list = itemEl.asJsonArray.map { el ->
                        ctx.deserialize<TourItem>(el, TourItem::class.java)
                    }
                    Items(list)
                }
                itemEl.isJsonObject -> {
                    val one = ctx.deserialize<TourItem>(itemEl, TourItem::class.java)
                    Items(listOf(one))
                }
                else -> Items(emptyList())
            }
        }

        // 비정상 케이스: items가 ""(문자열)로 오는 경우 → 빈 리스트 처리
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return Items(emptyList())
        }

        // 그 외 예외 케이스도 빈 리스트
        return Items(emptyList())
    }
}
