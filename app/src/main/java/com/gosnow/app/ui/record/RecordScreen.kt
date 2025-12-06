package com.gosnow.app.ui.record

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun RecordRoute(
    onBack: () -> Unit
) {
    val view = LocalView.current
    val activity = view.context as? Activity
    val window = activity?.window

    DisposableEffect(Unit) {
        if (window != null) {
            val originalStatusBarColor = window.statusBarColor
            val originalNavigationBarColor = window.navigationBarColor
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            val originalLightStatus = controller.isAppearanceLightStatusBars
            val originalLightNav = controller.isAppearanceLightNavigationBars

            window.statusBarColor = Color.Black.toArgb()
            window.navigationBarColor = Color.Black.toArgb()
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false

            onDispose {
                window.statusBarColor = originalStatusBarColor
                window.navigationBarColor = originalNavigationBarColor
                controller.isAppearanceLightStatusBars = originalLightStatus
                controller.isAppearanceLightNavigationBars = originalLightNav
            }
        } else {
            onDispose { }
        }
    }

    var isRecording by rememberSaveable { mutableStateOf(false) }

    RecordScreen(
        durationText = "00:00:00",
        distanceKm = 0.0,
        maxSpeedKmh = 0.0,
        verticalDropM = 0,
        isRecording = isRecording,
        onToggleRecording = { isRecording = !isRecording },
        onBack = onBack
    )
}

@Composable
fun RecordScreen(
    durationText: String,
    distanceKm: Double,
    maxSpeedKmh: Double,
    verticalDropM: Int,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF050505)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF8C8C8C)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "本次滑行时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8C8C8C)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = durationText,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "滑行里程",
                    value = String.format("%.1f km", distanceKm),
                    icon = Icons.Filled.TrendingUp
                )
                StatCard(
                    title = "最高速度",
                    value = String.format("%.0f km/h", maxSpeedKmh),
                    icon = Icons.Filled.Speed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "累计落差",
                    value = "$verticalDropM m",
                    icon = Icons.Outlined.Landscape
                )
                val cardColor = if (isRecording) Color(0xFFC6FF3F) else Color.White
                val contentColor = Color.Black
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .background(cardColor, shape = RoundedCornerShape(24.dp))
                        .clickable { onToggleRecording() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = contentColor
                        )
                        Text(
                            text = if (isRecording) "结束记录" else "开始记录",
                            color = contentColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            //.weight(1f)
            .height(88.dp)
            .background(color = Color(0xFF0F0F0F), shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color(0xFFB0B0B0),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
