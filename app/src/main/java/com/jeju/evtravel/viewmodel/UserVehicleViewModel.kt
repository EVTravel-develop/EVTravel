package com.jeju.evtravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// 사용자의 차량 정보를 담는 데이터 클래스
data class UserVehicleInfo(
    val carModel: String,
    val currentSoc: Int,
    val preferredSpeed: String // "급속", "완속", "초급속"
)

@HiltViewModel
class UserVehicleViewModel @Inject constructor() : ViewModel() {

    private val _vehicleInfo = MutableStateFlow<UserVehicleInfo?>(null)
    val vehicleInfo: StateFlow<UserVehicleInfo?> = _vehicleInfo

    fun updateVehicleInfo(carModel: String, currentSoc: Int, preferredSpeed: String) {
        // 실제 앱에서는 DataStore 등을 사용해 영구 저장하는 것이 좋습니다.
        _vehicleInfo.value = UserVehicleInfo(carModel, currentSoc, preferredSpeed)
    }
}