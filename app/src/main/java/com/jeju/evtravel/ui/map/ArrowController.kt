// app/src/main/java/com/jeju/evtravel/ui/map/ArrowController.kt
package com.jeju.evtravel.ui.map

import com.jeju.evtravel.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle

class ArrowController(
    private val map: KakaoMap,
    private val headingProvider: () -> Float = { 0f }, // 0..360, 북=0, 시계방향+
    private val iconBiasDeg: Float = 0f,
    private val layer: LabelLayer?
) {
    private var marker: Label? = null

    fun attachOrMove(position: LatLng) {
        val l = layer ?: return

        if (marker == null) {
            val style = LabelStyle
                .from(R.drawable.current_marker) // 한 장짜리 에셋
                .setAnchorPoint(0.5f, 0.5f) // 중심이 회전축
            marker = l.addLabel(
                LabelOptions.from(position)
                    .setStyles(style)
                    .setTag("location_marker")
            )
        } else {
            marker?.moveTo(position, 0)
        }

        // 붙인 직후 한 번 보정
        applyRotation()
    }

    fun onHeadingOrCameraChanged() {
        applyRotation()
    }

    fun detach() {
        marker?.remove()
        marker = null
    }

    private fun applyRotation() {
        val hDeg = normalizeDeg(headingProvider() + iconBiasDeg) // 도(deg)
        val camRotRad = map.cameraPosition?.rotationAngle ?: 0.0 // 라디안
        val displayRad = Math.toRadians(hDeg.toDouble()) - camRotRad
        marker?.rotateTo(displayRad.toFloat(), 0)
    }

    private fun normalizeDeg(d: Float): Float {
        var x = d % 360f
        if (x < 0f) x += 360f
        return x
    }
}