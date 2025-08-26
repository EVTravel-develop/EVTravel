package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class CoursePlaceList(
    val course_id: String = "",
    val place_id: String = ""
)