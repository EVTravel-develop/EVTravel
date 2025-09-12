package com.jeju.evtravel.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.jeju.evtravel.service.auth.FirebaseAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SummarizePlaceViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val functions = FirebaseFunctions.getInstance("us-central1")

    private val SUMMARY_KEY = "summary_text"

    private val _summaryText = MutableStateFlow(
        savedStateHandle.get<String>(SUMMARY_KEY) ?: "AI 작성 중..."
    )
    val summaryText: StateFlow<String> = _summaryText

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun setSummaryText(text: String) {
        _summaryText.value = text
        savedStateHandle[SUMMARY_KEY] = text
        _loading.value = false
    }

    fun getCachedSummary(): String? {
        return savedStateHandle.get<String>(SUMMARY_KEY)
    }

    // 장소 이름과 함께 x, y 좌표를 인자로 받도록 수정
    fun fetchPlaceSummary(placeName: String, x: String, y: String) {
        if (!FirebaseAuthService.isLoggedIn()) {
            setSummaryText("AI 요약 기능을 사용하려면 로그인이 필요합니다.")
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                val payload = hashMapOf(
                    "placeName" to placeName,
                    "x" to x,
                    "y" to y
                )

                val result = functions.getHttpsCallable("summarizePlace")
                    .call(payload)
                    .await()

                val data = result.data as? Map<String, Any?>
                val summary = data?.get("summary") as? String

                if (summary.isNullOrBlank()) {
                    setSummaryText("요약 정보를 불러올 수 없습니다.")
                } else {
                    setSummaryText(summary)
                }
            } catch (e: Exception) {
                if (e is FirebaseFunctionsException && e.code == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                    setSummaryText("인증 실패! 로그인을 다시 시도해주세요.")
                } else {
                    setSummaryText("요약 불러오기에 실패했습니다. 잠시 후 다시 시도해 주세요.")
                }
                Log.e("SummarizeViewModel", "Function call failed", e)
            }
        }
    }
}