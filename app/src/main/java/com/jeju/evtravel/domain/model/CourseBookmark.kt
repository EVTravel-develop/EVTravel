package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class CourseBookmark(
    val id: String = "",        // "$uid_$course_id"
    val uid: String = "",
    val course_id: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
