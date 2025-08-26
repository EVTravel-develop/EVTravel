package com.jeju.evtravel.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeju.evtravel.R

object RoundedBarDefaults {
    // SearchDestinationScreen과 동일 수치
    val Height = 51.dp
    val Shape = RoundedCornerShape(10.dp)
    val HorizontalPadding = 13.dp
    val VerticalPaddingTop = 13.dp
    val VerticalPaddingBottom = 11.dp
    val PlaceholderColor = Color(0xFF949494)
    // shadow 색상도 동일(spot/ambient)
    val ShadowSpot = Color(0xA09A9A9A)
    val ShadowAmbient = Color(0xA09A9A9A)
}

/**
 * 지도 오버레이용: 클릭만 처리(키보드/포커스 없음)
 * - SearchDestinationScreen의 검색바 외형과 동일한 카드/그림자/내부 패딩 적용
 * - 포커스 개념이 없으므로 아이콘은 'off' 고정
 */
@Composable
fun ClickableSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RoundedBarDefaults.Height)
            .shadow(
                elevation = 6.dp,
                spotColor = RoundedBarDefaults.ShadowSpot,
                ambientColor = RoundedBarDefaults.ShadowAmbient,
                shape = RoundedBarDefaults.Shape
            )
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedBarDefaults.Shape
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(
                start = RoundedBarDefaults.HorizontalPadding,
                end = RoundedBarDefaults.HorizontalPadding,
                top = RoundedBarDefaults.VerticalPaddingTop,
                bottom = RoundedBarDefaults.VerticalPaddingBottom
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            // SearchDestinationScreen과 동일한 비트맵 아이콘 사용
            Image(
                painter = painterResource(id = R.drawable.ic_search_off),
                contentDescription = "search icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(17.dp)
            )

            Text(
                text = placeholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 26.53.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight.W400,
                    color = RoundedBarDefaults.PlaceholderColor
                )
            )
        }
    }
}

/**
 * 입력 가능한 검색바: SearchScreen에서 사용
 * - SearchDestinationScreen의 BasicTextField + 아이콘/패딩/그림자/라운드/폰트/placeholder를 그대로 복제
 * - trailingClear=true면 X 버튼 노출
 */
@Composable
fun RoundedSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    onSearch: () -> Unit,
    trailingClear: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RoundedBarDefaults.Height)
            .shadow(
                elevation = 6.dp,
                spotColor = RoundedBarDefaults.ShadowSpot,
                ambientColor = RoundedBarDefaults.ShadowAmbient,
                shape = RoundedBarDefaults.Shape
            )
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedBarDefaults.Shape
            )
            .padding(
                start = RoundedBarDefaults.HorizontalPadding,
                end = RoundedBarDefaults.HorizontalPadding,
                top = RoundedBarDefaults.VerticalPaddingTop,
                bottom = RoundedBarDefaults.VerticalPaddingBottom
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            Image(
                painter = painterResource(
                    id = if (isFocused || value.isNotEmpty())
                        R.drawable.ic_search_on
                    else
                        R.drawable.ic_search_off
                ),
                contentDescription = "search icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(17.dp)
            )

            // 입력 영역
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.roboto)),
                        fontWeight = FontWeight.W400,
                        color = Color.Black
                    ),
                    interactionSource = interactionSource,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    modifier = Modifier.fillMaxWidth()
                )

                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 26.53.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight.W400,
                            color = RoundedBarDefaults.PlaceholderColor
                        )
                    )
                }
            }

            if (trailingClear && value.isNotEmpty()) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "지우기",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onValueChange("") }
                )
            }
        }
    }
}
