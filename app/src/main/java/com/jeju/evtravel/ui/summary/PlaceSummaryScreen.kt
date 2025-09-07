package com.jeju.evtravel.ui.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.detail.AiSummaryTextStyle
import com.jeju.evtravel.ui.detail.AiTitleTextStyle
import com.jeju.evtravel.ui.detail.InfoRow
import com.jeju.evtravel.ui.detail.TitleTextStyle
import com.jeju.evtravel.ui.theme.Variables

@Composable
fun PlaceSummaryScreen(
    place: Place,
    onNavigateClick: () -> Unit,
    onExpandToDetail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandToDetail),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = place.name,
                style = TitleTextStyle,
                maxLines = 1,                         // 한 줄만 표시
                overflow = TextOverflow.Ellipsis,     // 길면 … 처리
                modifier = Modifier.weight(1f)        // 오른쪽 아이콘 자리 확보
            )
            Icon(
                painterResource(id = R.drawable.ic_right),
                contentDescription = "상세 보기",
                tint = Color.Black,                   // 필요하면 색상 지정
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        val addr = place.roadAddress?.takeIf { it.isNotBlank() } ?: place.address
        if (!addr.isNullOrBlank()) {
            InfoRow(iconRes = R.drawable.ic_location, text = addr)
        }
        if (!place.phone.isNullOrBlank()) {
            InfoRow(iconRes = R.drawable.ic_phone, text = place.phone)
        }

        Spacer(Modifier.height(12.dp))

        AiSummaryBox(text = "AI 작성 중...")
        Spacer(Modifier.height(12.dp))

        PrimaryActionButton(text = "안내하기", onClick = onNavigateClick)
    }
}

/* ===== 장소에서 쓰는 작은 컴포넌트(필요 시 공용으로 뽑기) ===== */

@Composable
fun AiSummaryBox(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEBF4FF), shape = RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, Color(0xFFEBF4FF)), shape = RoundedCornerShape(12.dp))
            .padding(start = 7.dp, end = 7.dp, top = 6.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_ai),
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("AI 요약", style = AiTitleTextStyle, color = Variables.Blue700)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽 인용구 라인
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF8BBFFF))
            )
            Spacer(Modifier.width(8.dp))
            // 본문 텍스트
            Text(
                text,
                style = AiSummaryTextStyle,
                color = Color(0xFF535353)
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Variables.Blue700,
            contentColor = Color.White
        ),
    ) { Text(text) }
}
