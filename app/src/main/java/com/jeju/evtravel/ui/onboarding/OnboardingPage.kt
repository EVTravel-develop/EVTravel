// com/jeju/evtravel/ui/onboarding/OnboardingPage.kt
package com.jeju.evtravel.ui.onboarding

data class OnboardingPage(
    val titleNormalPart1: String,   // 일반 텍스트 (줄바꿈 포함)
    val titleBluePart: String,      // 파란색으로 강조할 텍스트
    val titleNormalPart2: String,   // 다시 일반 텍스트
    val subtitle: String,
    val imageRes: Int
)