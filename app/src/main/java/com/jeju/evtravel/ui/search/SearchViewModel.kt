package com.jeju.evtravel.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.jeju.evtravel.ui.map.DEFAULT_CENTER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : ViewModel() {

    enum class SearchType { PLACE, CHARGER }

    sealed interface SearchUiState {
        data object Idle : SearchUiState
        data object Loading : SearchUiState
        data class Success(val items: List<Place>) : SearchUiState
        data class Error(val message: String) : SearchUiState
    }

    // --- UI 입력 상태 ---
    val query = MutableStateFlow("")
    val type = MutableStateFlow(SearchType.PLACE)

    // 즉시 검색 트리거
    private val trigger = MutableStateFlow(0)
    fun forceSearch() { trigger.value += 1 }

    // 현재 위치/반경
    private val location = MutableStateFlow<Pair<Double, Double>?>(null) // (lon, lat)
    private val radius = MutableStateFlow(20000)

    fun setType(newType: SearchType) { type.value = newType }
    fun updateQuery(text: String) { query.value = text }
    fun setLocation(lon: Double, lat: Double) { location.value = lon to lat }
    fun setRadius(meters: Int) { radius.value = meters }

    // --- 쿼리 빌드 규칙 ---
    private fun buildQuery(t: SearchType, raw: String): String {
        val base = raw.trim().replace("\\s+".toRegex(), " ")
        return when (t) {
            SearchType.PLACE ->
                if (base.isBlank()) "전기차 충전소" else base
            SearchType.CHARGER -> listOf(base, "전기차 충전소")
                .filter { it.isNotBlank() }.joinToString(" ")
        }
    }

    // 입력 디바운스는 query에만 적용
    private val debouncedQuery = query.debounce(300)

    // --- 자동 검색 파이프라인 ---
    // 입력 변화(타이핑/토글/위치/반경)에 반응, 300ms 디바운스, 최신 요청만 살림(flatMapLatest)
    val uiState: StateFlow<SearchUiState> =
        combine(type, debouncedQuery, location, radius, trigger) { t, q, loc, r, tick ->
            Params(
                tick = tick,
                query = buildQuery(t, q), // PLACE/CHARGER 규칙 반영된 최종 쿼리
                rawQuery = q.trim(),               // 사용자 원문(빈 문자열 판정용)
                loc = loc,                         // 현재 위치 (null 가능)
                radius = r
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { params ->
                flow {
                    // 위치 없고, 사용자 입력도 비었으면 아무 것도 하지 않음
                    if (params.loc == null && params.rawQuery.isBlank()) {
                        emit(SearchUiState.Idle)
                        return@flow
                    }

                    emit(SearchUiState.Loading)

                    val result = runCatching {
                        if (params.rawQuery.isBlank()) {
                            val (lon, lat) = params.loc!!
                            searchNearbyPlacesUseCase(
                                query = params.query,
                                x = DEFAULT_CENTER.longitude,
                                y = DEFAULT_CENTER.latitude,
                                radius = params.radius
                            )
                        } else {
                            val loc = params.loc
                            searchNearbyPlacesUseCase(
                                query = params.query,
                                x = 0.0,
                                y = 0.0,
                                radius = params.radius
                            )
                        }
                    }

                    result.onSuccess { emit(SearchUiState.Success(it)) }
                        .onFailure { emit(SearchUiState.Error(it.message ?: "검색 실패(네트워크 또는 파라미터 확인)")) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchUiState.Idle
            )

    private data class Params(
        val tick: Int,
        val query: String,
        val rawQuery: String,
        val loc: Pair<Double, Double>?,
        val radius: Int
    )
}