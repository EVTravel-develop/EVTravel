package com.jeju.evtravel.ui.map

import com.jeju.evtravel.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle

fun KakaoMap.moveToLocationWithMarker(latLng: LatLng?, zoom: Int = 15) {
    latLng?.let {
        moveCamera(CameraUpdateFactory.newCenterPosition(it, zoom))
        labelManager?.layer?.apply {
            removeAll()
            val style = LabelStyle.from(R.drawable.current_location)
            val labelOptions = LabelOptions.from(it).setStyles(style)
            addLabel(labelOptions)
        }
    }
}