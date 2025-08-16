package com.jeju.evtravel.ui.map

import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle

fun Place.toLabelOptions(): LabelOptions {
    val latLng = LatLng.from(latitude, longitude)
    val style = LabelStyle.from(R.drawable.blue_marker)

    return LabelOptions.from(latLng)
        .setStyles(style)
        .setTag(id)
}

fun KakaoMap.addMarkers(
    currentLatLng: LatLng?,
    places: List<Place>,
    zoom: Int = 15
) {
    val layer = this.labelManager?.layer ?: return
    layer.removeAll() // 전체 삭제

    // 현재 위치 마커 추가
    currentLatLng?.let {
        // 지도 중심 이동 + 줌 레벨 유지
        moveCamera(CameraUpdateFactory.newCenterPosition(it, zoom))
        val style = LabelStyle.from(R.drawable.current_location)
        val option = LabelOptions.from(it).setStyles(style)
        layer.addLabel(option)
    }

    // 주변 장소 마커 추가
    places.forEach { place ->
        layer.addLabel(place.toLabelOptions())
    }
}