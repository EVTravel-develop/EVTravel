package com.jeju.evtravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeju.evtravel.ui.planner.*

class MainActivity : ComponentActivity() {
    private val plannerViewModel by viewModels<PlannerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EVTravelApp(plannerViewModel)
        }
    }
}

@Composable
fun EVTravelApp(viewModel: PlannerViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "planner") {
        composable("planner") {
            PlannerScreen(
                onCreatePlanClick = {
                    navController.navigate("calendar") // 달력 화면으로 이동
                }
            )
        }
        composable("calendar") {
            CalendarScreen(
                viewModel = viewModel,
                navController = navController,
                onNextClick = {
                    navController.navigate("editPlan") // 달력 화면에서 다음 일정 추가 화면으로 이동
                }
            )
        }
        composable("editPlan") {
            EditPlanScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditDateClick = { navController.navigate("calendar") }
            )
        }
    }
}