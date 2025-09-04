package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jeju.evtravel.R
import com.jeju.evtravel.ui.theme.Variables
import androidx.compose.ui.graphics.vector.ImageVector

val RobotoFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

val DetailTitleTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.001.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

val DetailSubtitleTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

val DetailLabelTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp
)

/* ------------------------------ Shared UI ------------------------------ */

@Composable
fun HeaderImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    fallbackRes: Int = R.drawable.placeholder_large // 프로젝트에 맞게 교체
) {
    Box(modifier = modifier.height(220.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = fallbackRes),
            placeholder = painterResource(id = fallbackRes)
        )
        // 상단/하단 살짝 어둡게
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x33000000),
                        1f to Color(0x11000000)
                    )
                )
        )
    }
}

@Composable
fun InfoCard(
    hours: String?,
    address: String?,
    phone: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (!hours.isNullOrBlank()) {
                InfoRow(
                    leading = { Icon(painterResource(id = R.drawable.ic_clock), contentDescription = null) },
                    text = hours
                )
                Spacer(Modifier.height(12.dp))
            }
            if (!address.isNullOrBlank()) {
                InfoRow(
                    leading = { Icon(painterResource(id = R.drawable.ic_location), contentDescription = null) },
                    text = address
                )
                Spacer(Modifier.height(12.dp))
            }
            if (!phone.isNullOrBlank()) {
                InfoRow(
                    leading = { Icon(painterResource(id = R.drawable.ic_phone), contentDescription = null) },
                    text = phone
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    leading: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            leading()
        }
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(text = title, style = DetailTitleTextStyle, modifier = modifier)
}

@Composable
fun ActionFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Variables.Blue700,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Outlined.NearMe, contentDescription = "길안내")
    }
}

/* ------------------------------ Utilities ------------------------------ */

fun isCharging(status: String?): Boolean {
    // 공공데이터 표준/내부코드 양쪽 대응
    return status == "3" || status.equals("CHARGING", true)
}



@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("다시 시도") }
        }
    }
}
