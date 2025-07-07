package com.jeju.evtravel.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.data.repository.PlanRepositoryImpl
import com.jeju.evtravel.domain.usecase.SavePlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 플래너 화면의 뷰모델 클래스
 * UI 상태를 관리하고 비즈니스 로직을 처리합니다.
 */
class PlannerViewModel : ViewModel() {
    // 의존성 주입 (실제 앱에서는 Hilt 등을 사용하여 주입)
    private val repository = PlanRepositoryImpl()
    private val savePlanUseCase = SavePlanUseCase(repository)

    // 여행 시작일 상태 (관찰 가능한 상태)
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate

    // 여행 종료일 상태 (관찰 가능한 상태)
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate

    /**
     * 여행 기간을 설정하는 메서드
     * 
     * @param start 여행 시작일
     * @param end 여행 종료일
     */
    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }

    /**
     * 현재 플랜을 저장하는 메서드
     * 
     * @param start 여행 시작일 (문자열 형식)
     * @param end 여행 종료일 (문자열 형식)
     * @param days 여행 일정 리스트
     */
    fun saveCurrentPlan(start: String, end: String, days: List<String>) {
        viewModelScope.launch {
            val plan = PlanDto(
                startDate = start,
                endDate = end,
                days = days
            )
            savePlanUseCase(plan)
        }
    }
}