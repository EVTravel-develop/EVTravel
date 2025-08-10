package com.jeju.evtravel.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jeju.evtravel.R

// 탭 정보 데이터 클래스
data class BottomNavItem(
    val name: String,
    val route: String,
    val iconRes: Int,
    val iconSelectedRes: Int
)

// 탭 리스트
val items = listOf(
    BottomNavItem(
        name = "홈",
        route = "map",
        iconRes = R.drawable.ic_home,
        iconSelectedRes = R.drawable.ic_home_on
    ),
    BottomNavItem(
        name = "플래너",
        route = "planner",
        iconRes = R.drawable.ic_plan,
        iconSelectedRes = R.drawable.ic_plan_on
    ),
    BottomNavItem(
        name = "마이",
        route = "my",
        iconRes = R.drawable.ic_my,
        iconSelectedRes = R.drawable.ic_my_on
    )
)

// BottomNavigationBar 컴포저블 함수
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val roundedShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    Box(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                spotColor = Color(0x40A7A7A7),
                ambientColor = Color(0x40A7A7A7),
                shape = roundedShape
            )
            .clip(roundedShape)
            .background(color = Color(0xFFFFFFFF))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ) {
            items.forEach { item ->
                val isSelected = when {
                    // 현재 아이템이 '플래너' 탭일 경우
                    item.route == "planner" -> {
                        // 현재 경로가 'planner' 또는 'planList'로 시작하면 선택된 것으로 간주
                        currentRoute?.startsWith("planner") == true || currentRoute?.startsWith("planList") == true
                    }
                    else -> currentRoute == item.route
                }
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (isSelected) item.iconSelectedRes else item.iconRes
                            ),
                            contentDescription = item.name,
                            tint = Color.Unspecified // 아이콘 색상 변경 방지
                        )
                    },
                    label = { Text(item.name) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Unspecified,
                        unselectedIconColor = Color.Unspecified,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent // 선택 시 배경 제거
                    )
                )
            }
        }
    }
}