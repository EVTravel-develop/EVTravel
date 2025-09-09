package com.jeju.evtravel.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SummarizePlaceViewModel : ViewModel() {
    private val functions = FirebaseFunctions.getInstance("us-central1")

    private val _summaryText = MutableStateFlow("AI 작성 중...")
    val summaryText: StateFlow<String> = _summaryText

    fun setSummaryText(text: String) {
        _summaryText.value = text
    }

    // 장소 이름과 함께 x, y 좌표를 인자로 받도록 수정
    fun fetchPlaceSummary(placeName: String, x: String, y: String) {
        viewModelScope.launch {
            try {
                // 1. 함수에 전달할 데이터를 Map 형태로 만듭니다.
                //    "x"와 "y" 키를 추가하여 함수가 기대하는 페이로드 형식을 맞춥니다.
                val payload = hashMapOf(
                    "placeName" to placeName,
                    "x" to x,
                    "y" to y
                )

                // 2. 함수를 호출하고 결과를 기다립니다.
                val result = functions.getHttpsCallable("summarizePlace")
                    .call(payload)
                    .await()

                // 3. 응답 데이터를 파싱합니다. (응답 형식은 이미 확인했으므로 이 코드는 그대로 둡니다.)
                val data = result.data as? Map<String, Any?>
                val summary = data?.get("summary") as? String

                if (summary.isNullOrBlank()) {
                    setSummaryText("요약 정보를 불러올 수 없습니다.")
                } else {
                    setSummaryText(summary)
                }
            } catch (e: Exception) {
                setSummaryText("요약 불러오기 실패: ${e.message}")
            }
        }
    }
}