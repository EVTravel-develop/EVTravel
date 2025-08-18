// com.jeju.evtravel.navigation.BottomNavGraph.kt
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

data class BottomNavItem(
    val name: String,
    val route: String,
    val iconRes: Int,
    val iconSelectedRes: Int
)

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

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val roundedShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Box(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                spotColor = Color(0x40A7A7A7),
                ambientColor = Color(0x40A7A7A7),
                shape = roundedShape
            )
            .clip(roundedShape)
            .background(color = Color.White)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ) {
            items.forEach { item ->
                val isSelected = when {
                    item.route == "planner" ->
                        currentRoute?.startsWith("planner") == true || currentRoute?.startsWith("planList") == true
                    item.route == "my" -> currentRoute == "my"
                    else -> currentRoute == item.route
                }
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (isSelected) item.iconSelectedRes else item.iconRes
                            ),
                            contentDescription = item.name,
                            tint = Color.Unspecified
                        )
                    },
                    label = { Text(item.name) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Unspecified,
                        unselectedIconColor = Color.Unspecified,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
