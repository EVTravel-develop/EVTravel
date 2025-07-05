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

class PlannerViewModel : ViewModel() {

    private val repository = PlanRepositoryImpl()
    private val savePlanUseCase = SavePlanUseCase(repository)

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }

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