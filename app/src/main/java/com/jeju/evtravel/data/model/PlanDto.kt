package com.jeju.evtravel.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * 플랜 정보를 담는 데이터 클래스
 * Firestore와의 데이터 전송에 사용됨
 */

// 날짜별 여행 계획 데이터 클래스
data class DayPlan(
    var date: String = "",  // yyyy-MM-dd
    var places: List<PlaceDto> = emptyList()
)

data class PlanDto(
    // Firestore 문서 ID (자동 생성됨)
    @DocumentId
    var id: String = "",

    // 플랜 제목
    var title: String = "",

    // 여행지
    var destination: String = "",

    // 여행 시작일
    var startDate: String = "",

    // 여행 종료일
    var endDate: String = "",

    // 여행 일정별 세부 계획을 담는 리스트
    var days: List<DayPlan> = emptyList(),

    // 사용자 ID
    var userId: String = "",

    // 생성 시각
    var createdAt: Timestamp = Timestamp.now(),

    // 수정 시각
    var updatedAt: Timestamp = Timestamp.now()
)