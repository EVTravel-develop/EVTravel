package com.jeju.evtravel.data.remote.mapper

import com.jeju.evtravel.data.remote.dto.PlaceDto
import com.jeju.evtravel.domain.model.Place

fun PlaceDto.toDomain(): Place =
    Place(
        id = id, // id
        name = placeName,  // place_name
        category = categoryName,
        //category_group_code	String
        //category_group_name	String
        //phone	String
        address = addressName, // address_name
        roadAddress = roadAddressName, // road_address_name
        longitude = x.toDoubleOrNull() ?: 0.0, // x
        latitude = y.toDoubleOrNull() ?: 0.0, // y
        url = placeUrl, // place_url
        phone = phone,
        distanceMeters = distance?.toIntOrNull() // distance
    )