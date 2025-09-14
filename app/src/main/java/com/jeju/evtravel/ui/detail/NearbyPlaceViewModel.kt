package com.jeju.evtravel.ui.detail

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.data.util.TourCategoryMaps.mapCat3
import com.jeju.evtravel.data.util.TourCategoryMaps.mapContentType
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.model.TourPlace
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.jeju.evtravel.domain.usecase.TourPlaceDetailUseCase
import com.jeju.evtravel.domain.usecase.TourPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    val itemsRaw: List<Place> = emptyList()
)

private const val TAG = "NearbyPlaceVM"

@HiltViewModel
class NearbyPlaceViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase,
    private val tourPlaceDetailUseCase: TourPlaceDetailUseCase,
    private val firestore: FirebaseFirestore
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

    private fun getCategoryQuery(category: String): String = when (category) {
        "자연환경" -> "관광지"
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

        viewModelScope.launch {
            runCatching {
                searchNearbyPlacesUseCase.invoke(
                    query = getCategoryQuery(_state.value.selectedCategory), // 카테고리명을 쿼리로 넘김
                    x = x,
                    y = y,
                    radius = lastRadius
                )
            }.onSuccess { kakaoPlaces ->
                Log.d(TAG, "Kakao API로 ${kakaoPlaces.size}개의 장소 검색 성공.")
                val placesWithImages = kakaoPlaces.map { place ->
                    async {
                        var imageUrl: String? = null
                        var contentId: String? = null

                        try {
                            val document =
                                firestore.collection("kakaoPlaceList").document(place.id).get()
                                    .await()
                            if (document.exists()) {
                                contentId = document.getString("contentId")
                                if (!contentId.isNullOrBlank()) {
                                    // 2-2. contentId로 Tour API 호출하여 이미지 URL 가져오기
                                    val tourDetail = tourPlaceDetailUseCase(contentId)
                                    if (tourDetail != null) {
                                        imageUrl = tourDetail.firstImage?.takeIf { it.isNotBlank() }
                                            ?: tourDetail.firstImage2
                                    }
                                } else {
                                    Log.w(TAG, "Firestore 문서에 contentId 필드가 없습니다: ${place.id}")
                                }
                            } else {
                                Log.w(TAG, "Firestore에서 kakaoPlaceList 문서 찾을 수 없음: ${place.id}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "장소(${place.name}) 이미지 URL 가져오기 실패", e)
                        }
                        place.copy(imageUrl = imageUrl)
                    }
                }.awaitAll() // 모든 비동기 작업이 완료될 때까지 대기

                val mapped = placesWithImages.map { it.toUi() }
                _state.value = _state.value.copy(
                    loading = false,
                    itemsAll = mapped,
                    items = mapped,
                    itemsRaw = placesWithImages
                )
                onComplete()
            }.onFailure { e ->
                _state.value =
                    _state.value.copy(loading = false, error = e.message ?: "정보를 불러오지 못했습니다.")
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
            imageUrl = this.imageUrl,
            contentId = null
        )
    }
}