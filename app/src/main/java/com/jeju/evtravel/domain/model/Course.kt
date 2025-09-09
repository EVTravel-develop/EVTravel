package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Keep
@IgnoreExtraProperties
@Parcelize
data class Course(
    val id: Long = 0,
    val place_name: String = "",
    val road_address_name: String = "",
    val address_name: String = "",
    val x: Double = 0.0,
    val y: Double = 0.0,
    val course_info: List<CourseDetail> = emptyList()
) : Parcelable

@Keep
@IgnoreExtraProperties
@Parcelize
data class CourseDetail(
    val course_name: String = "",
    val course_description: String = "",
    val places: List<String> = emptyList()
) : Parcelable