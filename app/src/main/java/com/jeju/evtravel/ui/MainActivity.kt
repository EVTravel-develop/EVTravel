package com.jeju.evtravel.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jeju.evtravel.ui.planner.PlannerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlannerScreen(
                onCreatePlanClick = {
                    // TODO: 달력 화면 이동 (나중에)
                }
            )
        }
    }
}