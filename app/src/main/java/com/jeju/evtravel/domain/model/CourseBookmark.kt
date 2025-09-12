package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class CourseBookmark(
    val id: String = "",        // "$uid_$course_id"
    val uid: String? = "",
    val course_id: String = "",
    val course_name: String = "", // 코스 이름 추가
    val course_description: String = "", // 코스 설명 추가
    val imageUrl: String = "",    // 썸네일 이미지 URL 추가
    val createdAt: Timestamp = Timestamp.now()
)