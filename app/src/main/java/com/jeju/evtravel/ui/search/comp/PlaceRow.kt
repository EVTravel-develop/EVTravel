package com.jeju.evtravel.ui.search.comp

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.domain.model.Place

@Composable
fun PlaceRow(
    place: Place,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(place.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            val addr = place.roadAddress?.takeIf { it.isNotBlank() }
                ?: place.address?.takeIf { it.isNotBlank() }
                ?: ""
            Text(addr, style = MaterialTheme.typography.bodySmall)
            Log.d("place_name:", place.name)
            Log.d("road_addr:", place.roadAddress.toString())
            Log.d("place_addr:", place.address.toString())
        }
    }
}