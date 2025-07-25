package com.jeju.evtravel.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.data.model.DayPlan
import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.data.model.PlaceDto
import com.jeju.evtravel.data.repository.PlanRepositoryImpl
import com.jeju.evtravel.domain.usecase.SavePlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.usecase.SearchPlaceUseCase
import com.jeju.evtravel.data.repository.PlaceRepositoryImpl

/**
 * 플래너 화면의 뷰모델 클래스
 * UI 상태를 관리하고 비즈니스 로직을 처리합니다.
 */
class PlannerViewModel : ViewModel() {
    // 의존성 주입 (실제 앱에서는 Hilt 등을 사용하여 주입)
    private val repository = PlanRepositoryImpl()
    private val savePlanUseCase = SavePlanUseCase(repository)

    // 날짜별 DayPlan 상태
    private val _dayPlans = MutableStateFlow<List<DayPlan>>(emptyList())
    val dayPlans: StateFlow<List<DayPlan>> = _dayPlans

    // 현재 선택된 날짜 (편집할 날짜)
    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate

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

    private val searchPlaceUseCase = SearchPlaceUseCase(PlaceRepositoryImpl())

    private val _searchResults = MutableStateFlow<List<Place>>(emptyList())
    val searchResults: StateFlow<List<Place>> = _searchResults

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList() // 빈 문자열이면 결과 초기화
            } else {
                val results = searchPlaceUseCase(query)
                _searchResults.value = results
            }
        }
    }

    /**
     * 현재 플랜을 저장하는 메서드
     */
    fun saveCurrentPlan(start: String, end: String) {
        viewModelScope.launch {
            val plan = PlanDto(
                startDate = start,
                endDate = end,
                days = _dayPlans.value,  // 날짜별 DayPlan 객체들 그대로 저장
                userId = "somi"      // 실제 로그인 사용자 ID로 대체
            )
            savePlanUseCase(plan)
        }
    }

    /**
     * 날짜를 선택합니다.
     * @param date 선택할 날짜 (yyyy-MM-dd 형식)
     */
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    /**
     * CalendarScreen에서 날짜 범위를 선택하면 DayPlan 리스트 초기화
     * @param start 여행 시작일
     * @param end 여행 종료일
     */
    fun initDayPlans(start: LocalDate, end: LocalDate) {
        val days = generateSequence(start) { d ->
            val next = d.plusDays(1)
            if (!next.isAfter(end)) next else null
        }.map { DayPlan(date = it.toString()) }.toList()
        _dayPlans.value = days
    }

    /**
     * 특정 날짜에 장소를 추가합니다.
     * @param date 날짜 (yyyy-MM-dd 형식)
     * @param place 추가할 장소
     */
    fun addPlaceToDate(date: String, place: PlaceDto) {
        _dayPlans.value = _dayPlans.value.map { dayPlan ->
            if (dayPlan.date == date) {
                dayPlan.copy(places = dayPlans.value.find { it.date == date }?.places.orEmpty() + place)
            } else dayPlan
        }
    }

    // 특정 날짜의 장소 삭제
    fun removePlaceFromDate(date: String, placeId: String) {
        _dayPlans.value = _dayPlans.value.map { dayPlan ->
            if (dayPlan.date == date) {
                dayPlan.copy(places = dayPlan.places.filterNot { it.id == placeId })
            } else dayPlan
        }
    }

    fun reorderPlaces(date: String, fromIndex: Int, toIndex: Int) {
        _dayPlans.value = _dayPlans.value.map { dayPlan ->
            if (dayPlan.date == date) {
                val mutablePlaces = dayPlan.places.toMutableList()
                val moved = mutablePlaces.removeAt(fromIndex)
                mutablePlaces.add(toIndex, moved)
                dayPlan.copy(places = mutablePlaces)
            } else dayPlan
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

}