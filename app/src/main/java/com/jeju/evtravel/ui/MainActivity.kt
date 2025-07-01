package com.jeju.evtravel.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeju.evtravel.ui.planner.CalendarScreen
import com.jeju.evtravel.ui.planner.PlannerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EVTravelApp()
        }
    }
}

@Composable
fun EVTravelApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "planner"
    ) {
        composable("planner") {
            PlannerScreen(
                onCreatePlanClick = {
                    navController.navigate("calendar") // 달력 화면으로 이동
                }
            )
        }
        composable("calendar") {
            CalendarScreen(
                onNextClick = {
                    navController.navigate("destination") // 달력 화면에서 다음 일정 추가 화면으로 이동
                }
            )
        }
    }
}