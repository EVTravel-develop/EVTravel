package com.jeju.evtravel.data.repository


import android.util.Log
import com.jeju.evtravel.BuildConfig
import com.jeju.evtravel.data.remote.api.KakaoLocalRegionApi
import com.jeju.evtravel.data.util.zcodeMap
import com.jeju.evtravel.domain.model.RegionCode
import com.jeju.evtravel.data.util.zscodeMap // 당신이 만든 구/시 -> 코드 map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionCodeRepository @Inject constructor(
    private val kakaoLocalApi: KakaoLocalRegionApi,
    private val restApiKey: String
) {
    suspend fun getRegionCodeFromCoord(x: Double, y: Double): RegionCode? {

        val res = kakaoLocalApi.getRegionCode(
            authorization = "KakaoAK $restApiKey",
            x = x, y = y
        )

        // 우선순위: region_type = "B"(법정동) > "H"(행정동)
        val target = res.documents.firstOrNull { it.regionType == "B" }
            ?: res.documents.firstOrNull { it.regionType == "H" }
            ?: return null.also {
                Log.w("RegionCode", "coord2region empty docs for ($x, $y)")
            }

        val top = normalizeTopName(target.region1)     // 예: "서울시" → "서울특별시"
        val mid = target.region2.trim()                // 예: "관악구", "성남시"

        val zcode = zcodeMap[top]
        if (zcode == null) {
            Log.w("RegionCode", "zcode not found for top=$top (raw=${target.region1})")
            return null
        }

        val zscode = zscodeMap[mid]
        if (zscode == null) {
            Log.w("RegionCode", "zscode not found for mid=$mid (top=$top)")
            return null
        }

        return RegionCode(zcode = zcode, zscode = zscode)
    }

    /** "서울시", "부산시" 등을 환경부 표준인 "서울특별시", "부산광역시"로 보정 */
    private fun normalizeTopName(name: String): String {
        val n = name.trim()
        return when {
            n == "서울시" -> "서울특별시"
            n == "부산시" -> "부산광역시"
            n == "대구시" -> "대구광역시"
            n == "인천시" -> "인천광역시"
            n == "광주시" -> "광주광역시"
            n == "대전시" -> "대전광역시"
            n == "울산시" -> "울산광역시"
            n == "제주시" -> "제주특별자치도"
            else -> n
        }
    }
}