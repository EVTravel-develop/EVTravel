package com.jeju.evtravel.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.ui.detail.NavigationAppButtonTextStyle
import com.jeju.evtravel.ui.detail.NavigationAppTextStyle
import com.jeju.evtravel.ui.theme.Variables
import com.kakao.vectormap.LatLng

private fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationAppBottomSheet(
    context: Context,
    startLocation: LatLng?,
    endLocation: LatLng,
    destinationAddress: String,
    onDismiss: () -> Unit
) {
    var selectedApp by remember { mutableStateOf<NavigationApp?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            CustomDragHandle()

            // 앱 목록
            navigationApps.forEach { app ->
                NavigationAppItem(
                    app = app,
                    onClick = {
                        selectedApp = app
                    },
                    isSelected = app == selectedApp
                )
            }

            // 완료 버튼
            Button(
                onClick = {
                    selectedApp?.let { app ->
                        if (isAppInstalled(context, app.packageName)) {
                            // 설치됨 -> 길 안내 실행
                            launchNavigationApp(context, app, startLocation, endLocation, destinationAddress)
                        } else {
                            // 미설치 -> 플레이 스토어로 이동
                            val marketUri = Uri.parse("market://details?id=${app.packageName}")
                            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                            context.startActivity(marketIntent)
                        }
                    }
                    onDismiss()
                },
                enabled = selectedApp != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Variables.Blue700,
                    contentColor = Color.White,
                    disabledContainerColor = Variables.Grayscale300,
                    disabledContentColor = Color.White,
                )
            ) {
                Text(
                    text = "완료",
                    style = NavigationAppButtonTextStyle,
                )
            }
        }
    }
}

@Composable
fun CustomDragHandle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color = Variables.Grayscale300)
        )
    }
}


@Composable
fun NavigationAppItem(
    app: NavigationApp,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Variables.Grayscale50 else Color.White,
        border = if (isSelected) BorderStroke(2.dp, Variables.Blue700) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = app.name,
                style = NavigationAppTextStyle,
                color = if (isSelected) Variables.Blue700 else Color.Black
            )
        }
    }
}