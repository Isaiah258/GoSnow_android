package com.gosnow.app.ui.record

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosnow.app.ui.record.BasicSessionRecorder
import com.gosnow.app.recording.RecordingViewModel
import com.gosnow.app.recording.location.SystemLocationService
import com.gosnow.app.recording.metrics.BasicMetricsComputer
import com.gosnow.app.ui.record.storage.FileSessionStore

/**
 * 外层入口：处理状态栏图标颜色 + 注入真正的 RecordingViewModel
 */
@Composable
fun RecordRoute(
    onBack: () -> Unit = {}
) {
    // 把状态栏 / 导航栏的图标改成白色（背景是深色时更清晰）
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        val window = activity.window

        DisposableEffect(Unit) {
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            val oldLightStatus = controller.isAppearanceLightStatusBars
            val oldLightNav = controller.isAppearanceLightNavigationBars

            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false

            onDispose {
                controller.isAppearanceLightStatusBars = oldLightStatus
                controller.isAppearanceLightNavigationBars = oldLightNav
            }
        }
    }

    val context = LocalContext.current

    // 使用真正的记录逻辑 + 本地存储
    val vm: RecordingViewModel = viewModel(
        factory = RecordingViewModel.provideFactory(
            recorder = provideRecorder(context),
            store = FileSessionStore(context)
        )
    )

    RecordScreen(
        durationText = vm.durationText,
        distanceKm = vm.distanceKm,
        maxSpeedKmh = vm.maxSpeedKmh,
        verticalDropM = vm.verticalDropM,
        isRecording = vm.isRecording,
        onToggleRecording = { vm.onToggleRecording() },
        onBack = onBack
    )
}

private fun provideRecorder(context: Context): BasicSessionRecorder {
    val locationService = SystemLocationService(context)
    val metrics = BasicMetricsComputer()
    return BasicSessionRecorder(locationService, metrics)
}

/**
 * 记录页 UI —— 使用你满意那一版的布局 & 尺寸
 */
@Composable
fun RecordScreen(
    durationText: String,
    distanceKm: Double,
    maxSpeedKmh: Double,
    verticalDropM: Int,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = Color(0xFF050505)
    val cardColor = Color(0xFF151515)
    val accentGreen = Color(0xFFC6FF3F)

    Column(
        modifier = modifier
            .fillMaxSize()
            // 背景铺满到系统栏
            .background(background)
            // 内容避开状态栏 / 手势区域，但背景仍然铺满
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // 顶部：返回箭头
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 中间：本次滑行时间（无卡片，直接黑色背景）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = Color(0xFFBBBBBB),
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "本次滑行时间",
                color = Color(0xFFB0B0B0),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = durationText,
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
        }

        // 把下方四个胶囊推到底部
        Spacer(modifier = Modifier.weight(1f))

        // 底部 2×2 胶囊网格
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 第一行：里程 + 最高速度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    background = cardColor,
                    icon = Icons.Outlined.TrendingUp,
                    iconTint = Color(0xFFCCCCCC),
                    label = "滑行里程",
                    value = String.format("%.1f km", distanceKm)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    background = cardColor,
                    icon = Icons.Outlined.Speed,
                    iconTint = Color(0xFFCCCCCC),
                    label = "最高速度",
                    value = String.format("%.0f km/h", maxSpeedKmh)
                )
            }

            // 第二行：累计落差 + 开始/结束
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    background = cardColor,
                    icon = Icons.Outlined.Landscape,   // 山的图标
                    iconTint = Color(0xFFCCCCCC),
                    label = "累计落差",
                    value = "$verticalDropM m"
                )

                StartStopCard(
                    modifier = Modifier.weight(1f),
                    isRecording = isRecording,
                    onClick = onToggleRecording,
                    shape = RoundedCornerShape(26.dp),
                    idleBackground = Color.White,
                    activeBackground = accentGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))
    }
}

/**
 * 通用统计胶囊卡片
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    shape: Shape,
    background: Color,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier
            .height(120.dp), // 胶囊高度保持较大
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()                         // ✅ 占满整个 Card，高度不再是 wrapContent
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically // ✅ 在 Card 内垂直居中内容
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFF1F1F1F)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = label,
                    color = Color(0xFFB0B0B0),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}


/**
 * 右下角开始/结束胶囊
 */
@Composable
private fun StartStopCard(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    idleBackground: Color,
    activeBackground: Color
) {
    val bg = if (isRecording) activeBackground else idleBackground
    val text = if (isRecording) "结束记录" else "开始记录"
    val icon = if (isRecording) Icons.Filled.Stop else Icons.Filled.PlayArrow
    val textColor = Color.Black

    Card(
        modifier = modifier
            .height(120.dp), // ✅ 跟 StatCard 一致再大一点
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = bg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(26.dp) // ✅ 按钮图标也再大一点
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,          // ✅ 与统计值统一放大
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
