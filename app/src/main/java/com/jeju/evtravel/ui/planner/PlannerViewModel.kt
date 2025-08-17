package com.jeju.evtravel.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.BuildConfig
import com.jeju.evtravel.data.model.DayPlan
import com.jeju.evtravel.data.model.PlaceDto
import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.data.remote.RetrofitInstance
import com.jeju.evtravel.data.repository.ChargerRepositoryImpl
import com.jeju.evtravel.data.repository.PlaceRepositoryImpl
import com.jeju.evtravel.data.repository.PlanRepositoryImpl
import com.jeju.evtravel.domain.usecase.SavePlanUseCase
import com.jeju.evtravel.domain.usecase.SearchPlaceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * PlannerViewModel은 여행 계획을 관리하는 ViewModel입니다.
 * 여행 기간 설정, 장소 검색, 플랜 저장 등의 기능을 제공합니다.
 */
class PlannerViewModel : ViewModel() {
    // 의존성 주입 (실제 앱에서는 Hilt 등을 사용하여 주입)
    private val repository = PlanRepositoryImpl()
    private val savePlanUseCase = SavePlanUseCase(repository)
    
    // 현재 로그인된 사용자의 ID를 저장할 변수
    private var currentUserId: String? = null
    
    // 날짜별 DayPlan 상태
    private val _dayPlans = MutableStateFlow<List<DayPlan>>(emptyList())
    val dayPlans: StateFlow<List<DayPlan>> = _dayPlans
    
    // 현재 선택된 날짜 (편집할 날짜)
    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate
    
    // 여행 시작일 상태
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate
    
    // 여행 종료일 상태
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate
    
    // 플랜 목록 상태: null은 로딩 중, 빈 리스트는 플랜 없음, 리스트가 있으면 플랜 있음
    private val _plans = MutableStateFlow<List<PlanDto>?>(null)
    val plans: StateFlow<List<PlanDto>?> = _plans
    
    // 장소 검색을 위한 UseCase와 상태
    private val searchPlaceUseCase = SearchPlaceUseCase(
        PlaceRepositoryImpl(
            api = RetrofitInstance.kakaoLocalApi,
            restApiKey = BuildConfig.KAKAO_REST_API_KEY
        )
    )
    
    // 충전소 검색을 위한 ChargerRepository
    private val chargerRepository = ChargerRepositoryImpl(
        api = RetrofitInstance.kakaoLocalApi,
        restApiKey = BuildConfig.KAKAO_REST_API_KEY
    )
    
    // 장소 검색 결과 상태
    private val _searchResults = MutableStateFlow<List<UiPlace>>(emptyList())
    
    // 검색 결과를 UI에서 사용할 수 있도록 UiPlace로 변환하여 저장
    val searchResults: StateFlow<List<UiPlace>> = _searchResults
    
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
     * 장소 검색 메서드
     * @param query 검색어
     */
    fun searchPlaces(query: String, x: Double, y: Double) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList() // 빈 문자열이면 결과 초기화
            } else {
                val results = searchPlaceUseCase(query, x, y)
                _searchResults.value = results.map { UiPlace(place = it) }
            }
        }
    }
    
    /**
     * 현재 플랜을 저장합니다.
     * @param start 여행 시작일 (yyyy-MM-dd 형식)
     * @param end 여행 종료일 (yyyy-MM-dd 형식)
     */
    fun saveCurrentPlan(
        start: String,
        end: String,
        onSaveComplete: () -> Unit
    ) {
        // 저장된 userId를 사용, 없으면 함수 종료
        val userId = currentUserId ?: return
        
        viewModelScope.launch {
            val plan = PlanDto(
                startDate = start,
                endDate = end,
                days = _dayPlans.value,  // 날짜별 DayPlan 객체들 그대로 저장
                userId = userId
            )
            
            savePlanUseCase(plan,
                onSuccess = {
                    // 저장이 성공하면, 플랜 목록을 다시 불러옵니다.
                    viewModelScope.launch {
                        _plans.value = repository.getPlans(userId)
                        // 목록 로드까지 완료되면, 파라미터로 받은 콜백(화면 전환)을 실행
                        onSaveComplete()
                    }
                }
            )
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
                dayPlan.copy(places = dayPlan.places + place)
            } else dayPlan
        }
    }
    
    /**
     * 특정 날짜에서 장소를 제거합니다.
     * @param date 날짜 (yyyy-MM-dd 형식)
     * @param placeId 제거할 장소의 ID
     */
    fun removePlaceFromDate(date: String, placeId: String) {
        _dayPlans.value = _dayPlans.value.map { dayPlan ->
            if (dayPlan.date == date) {
                dayPlan.copy(places = dayPlan.places.filterNot { it.id == placeId })
            } else dayPlan
        }
    }
    
    /**
     * 특정 날짜의 장소를 재정렬합니다.
     * @param date 날짜 (yyyy-MM-dd 형식)
     * @param fromIndex 이동할 장소의 현재 인덱스
     * @param toIndex 이동할 장소의 목표 인덱스
     */
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
    
    /**
     * Firestore에서 플랜 목록을 불러옵니다.
     * @param userId 사용자 ID
     */
    fun loadPlans(userId: String) {
        // ViewModel에 현재 사용자 ID 저장
        this.currentUserId = userId
        viewModelScope.launch {
            _plans.value = repository.getPlans(userId)
        }
    }
    
    /**
     * 특정 플랜을 삭제합니다.
     * @param planId 삭제할 플랜의 ID
     */
    fun deletePlan(planId: String) {
        // 저장된 userId를 사용, 없으면 함수 종료
        val userId = currentUserId ?: return
        viewModelScope.launch {
            try {
                repository.deletePlan(planId)
                loadPlans(userId) // 삭제 후 플랜 목록 갱신
            } catch (e: Exception) {
                println("플랜 삭제 중 오류 발생: ${e.message}")
            }
        }
    }
    
    /**
     * 기존 PlanDto 객체를 받아 ViewModel의 상태를 설정합니다.
     * (플랜 조회 또는 수정 시 사용)
     * @param plan 화면에 표시할 플랜 데이터
     */
    fun loadPlanDetails(plan: PlanDto) {
        _startDate.value = LocalDate.parse(plan.startDate)
        _endDate.value = LocalDate.parse(plan.endDate)
        _dayPlans.value = plan.days
    }
    
    /**
     * ViewModel의 상태를 초기화합니다.
     * (새로운 플랜 생성 시작 시 사용)
     */
    fun clearPlanDetails() {
        _startDate.value = null
        _endDate.value = null
        _dayPlans.value = emptyList()
        _selectedDate.value = null
        _searchResults.value = emptyList()
    }
    
    fun toggleChargerSection(placeId: String) {
        viewModelScope.launch {
            _searchResults.value = _searchResults.value.map { item ->
                if (item.place.id == placeId) {
                    val expanded = !item.isExpanded
                    val chargers = if (expanded) {
                        chargerRepository.getNearbyChargers(
                            lat = item.place.latitude,
                            lon = item.place.longitude
                        ).take(5)
                    } else null
                    
                    item.copy(
                        isExpanded = expanded,
                        chargers = chargers
                    )
                } else item.copy(isExpanded = false)
            }
        }
    }
}