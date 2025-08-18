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
    val style = LabelStyle.from(R.drawable.ev_marker)

    return LabelOptions.from(latLng)
        .setStyles(style)
        .setTag(id)
}