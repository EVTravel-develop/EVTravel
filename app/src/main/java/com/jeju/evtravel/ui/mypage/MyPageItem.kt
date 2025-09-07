package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // ✅ Import ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeju.evtravel.R

@Composable
fun MyPageItem(
    icon: ImageVector, // ✅ Changed type from String to ImageVector
    text: String,
    onClick: () -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Now this line works correctly because 'icon' is an ImageVector
        Icon(imageVector = icon, contentDescription = text, tint = Color(0xFF424242))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 15.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.right),
            contentDescription = null,
            tint = Color.Gray
        )
    }
}