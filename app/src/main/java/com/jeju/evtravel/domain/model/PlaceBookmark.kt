package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class PlaceBookmark(
    val id: String = "",          // "$uid_$kakao_id"
    val uid: String? = "",         // User.uid
    val kakao_id: String = "",    // Kakao Map Place ID
    val x: Double = 0.0,          // 경도
    val y: Double = 0.0,          // 위도
    val place_name: String = "",
    val description: String = "",
    val image_url: String? = null
)
