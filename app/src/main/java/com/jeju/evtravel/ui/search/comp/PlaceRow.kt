package com.jeju.evtravel.ui.search.comp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import java.util.Locale

@Composable
fun PlaceRow(
    place: Place,
    query: String,
    onClick: () -> Unit
) {
    val name = place.name
    val addr = (place.roadAddress?.takeIf { it.isNotBlank() }
        ?: place.address?.takeIf { it.isNotBlank() }
        ?: "")

    // 표시 문자열: "이름 (주소)"
    val fullText = if (addr.isNotEmpty()) "$name ($addr)" else name

    // 🔹 검색어 하이라이트(대소문자 무시, 한글도 그대로 매칭)
    val annotated = buildAnnotatedString {
        val lowerFull = fullText.lowercase(Locale.getDefault())
        val lowerQuery = query.lowercase(Locale.getDefault()).trim()

        if (lowerQuery.isNotEmpty()) {
            var start = 0
            while (start < fullText.length) {
                val idx = lowerFull.indexOf(lowerQuery, startIndex = start)
                if (idx == -1) {
                    append(fullText.substring(start))
                    break
                }
                // 앞쪽 일반 텍스트
                append(fullText.substring(start, idx))
                // 매칭 구간 파란색
                withStyle(
                    SpanStyle(
                        color = Color(0xFF0173FF),         // Google Blue 느낌
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(fullText.substring(idx, idx + lowerQuery.length))
                }
                start = idx + lowerQuery.length
            }
        } else {
            append(fullText)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 검색 아이콘
        Image(
            painter = painterResource(id = R.drawable.ic_search_off),
            contentDescription = "search icon",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(17.dp),
            colorFilter = ColorFilter.tint(Color(0xFF9AA0A6))
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = annotated,
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF000000),
                ),
                maxLines = 1
            )
        }
    }
}