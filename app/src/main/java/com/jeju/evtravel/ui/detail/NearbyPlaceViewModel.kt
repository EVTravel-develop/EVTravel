package com.jeju.evtravel.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.data.util.TourCategoryMaps.mapCat3
import com.jeju.evtravel.data.util.TourCategoryMaps.mapContentType
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.model.TourPlace
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.jeju.evtravel.domain.usecase.TourPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI에서 그리드 카드에 쓸 데이터 */
data class UiNearbyPlace(
    val id: String,
    val title: String,
    val tags: String?,
    val imageUrl: String?,
    val contentId: String?   // 로컬 필터링용
)

data class NearbyUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "자연환경",
    val items: List<UiNearbyPlace> = emptyList(),     // 화면에 표시되는 목록(필터 적용 결과)
    val itemsAll: List<UiNearbyPlace> = emptyList(),  // 전체 원본(필터 전)
)

@HiltViewModel
class NearbyPlaceViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : ViewModel() {
    private var lastX: Double? = null
    private var lastY: Double? = null
    private var lastRadius: Int = 2000
    private val _state = MutableStateFlow(NearbyUiState())
    val state: StateFlow<NearbyUiState> = _state

    // 충전소에서 좌표로 주변 추천 장소 검색
    /** 충전소로부터 좌표 받아 주변 장소 조회 (1페이지, 거리순) */
    /** 충전소로부터 좌표 받아 주변 장소 조회 (1페이지, 거리순) */
    fun selectTourPlace(place: Place, onComplete: () -> Unit = {}) {
        lastX = place.longitude
        lastY = place.latitude
        fetch(onComplete)
    }

    /** 반경 변경(옵션) → 즉시 재조회 */
    fun setRadius(radiusM: Int) {
        if (lastRadius == radiusM) return
        lastRadius = radiusM
        fetch()
    }

    /** 카테고리 선택 → 서버 파라미터로 재조회 */
    fun selectCategory(label: String) {
        if (_state.value.selectedCategory == label) return
        _state.value = _state.value.copy(selectedCategory = label)
        fetch()
    }

    // -------------------- 내부 헬퍼 --------------------

    /** 선택된 카테고리에 맞춰 서버 파라미터(contentTypeId, cat1/2/3) 구성 */
//    private data class ServerFilter(
//        val contentTypeId: String? = null,
//        val cat1: String? = null,
//        val cat2: String? = null,
//        val cat3: String? = null,
//    )
//
//    private fun buildServerFilterFor(label: String): ServerFilter = when (label) {
//        "자연환경" -> ServerFilter(contentTypeId = "12") // 관광지
//        "박물관"   -> ServerFilter(contentTypeId = "14") // 문화시설
//        "맛집"     -> ServerFilter(contentTypeId = "39") // 음식점 전체
//        "카페"     -> ServerFilter(
//            contentTypeId = "39",         // 음식점
//            cat1 = "A05",                  // 대분류
//            cat2 = "A0502",                // 중분류
//            cat3 = "A05020900"             // 소분류: 카페/전통찻집
//        )
//        else -> ServerFilter()
//    }
    private fun getCategoryQuery(category: String): String = when (category) {
        "자연환경" -> "공원"
        "박물관" -> "박물관"
        "맛집" -> "맛집"
        "카페" -> "카페"
        else -> category
    }

    /** 공통 조회 로직 */
    fun fetch(onComplete: () -> Unit = {}) {
        val x = lastX
        val y = lastY
        if (x == null || y == null) return

        _state.value = _state.value.copy(loading = true, error = null)
//        val filter = buildServerFilterFor(_state.value.selectedCategory)

        viewModelScope.launch {
            runCatching {
                searchNearbyPlacesUseCase.invoke(
                    query = getCategoryQuery(_state.value.selectedCategory), // 카테고리명을 쿼리로 넘김
                    x = x,
                    y = y,
                    radius = lastRadius,
                    page = 1,
                    size = 15
                )
            }.onSuccess { list ->
                val mapped = list.map { it.toUi() }
                _state.value = _state.value.copy(
                    loading = false,
                    itemsAll = mapped,
                    items = mapped
                )
                onComplete()
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message ?: "정보를 불러오지 못했습니다.")
                onComplete()
            }
        }
    }

    init {
        fetch()
    }

    /** Domain → UI 매핑 */
    private fun Place.toUi(): UiNearbyPlace {
        return UiNearbyPlace(
            id = this.id,
            title = this.name,
            tags = this.category,
            imageUrl = null,
            contentId = null
        )
    }
}