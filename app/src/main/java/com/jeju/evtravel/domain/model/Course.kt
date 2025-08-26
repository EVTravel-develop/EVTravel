package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class Course(
    val id: String = "",
    val course_place_name1: String = "",
    val course_place_name2: String = "",
    val course_place_name3: String = "",
    val course_place_name4: String = "",
    val course_place_name5: String = "",
    val course_description: String = ""
)
