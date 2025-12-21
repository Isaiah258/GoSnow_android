package com.gosnow.app.ui.record

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosnow.app.ui.record.classifier.MotionMode
//import com.mapbox.geojson.Point
//import com.mapbox.maps.CameraOptions
//import com.mapbox.maps.MapView
//import com.mapbox.maps.plugin.LocationPuck2D
//import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
//import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
//import com.mapbox.maps.plugin.locationcomponent.location
// 添加到文件头部 imports
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport

@Composable
fun RecordRoute(
    onBack: () -> Unit
) {
    val view = LocalView.current
    val activity = view.context as? Activity
    val window = activity?.window
    val context = LocalContext.current

    val vm: RecordingViewModel = viewModel()

    // 标准：页面可见 bind，离开 unbind
    DisposableEffect(Unit) {
        vm.bindService()
        onDispose { vm.unbindService() }
    }

    // ---------- 权限 ----------
    var hasLocationPermission by remember { mutableStateOf(false) }  // coarse 或 fine 任一即可（给地图蓝点用）
    var hasFineLocation by remember { mutableStateOf(false) }        // 精确定位（给记录用）
    var hasNotificationPermission by remember { mutableStateOf(true) } // < 33 默认 true

    fun checkFineLocation(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkLocationPermission(): Boolean {
        val fine = checkFineLocation()
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasFineLocation = checkFineLocation()
        hasLocationPermission = checkLocationPermission()
        hasNotificationPermission = checkNotificationPermission()
    }

    // 进入页面自动补齐权限（成熟做法）
    LaunchedEffect(Unit) {
        hasFineLocation = checkFineLocation()
        hasLocationPermission = checkLocationPermission()
        hasNotificationPermission = checkNotificationPermission()

        val toRequest = buildList {
            // 地图蓝点：coarse/fine 任一即可，所以只要没有任何定位权限就请求
            if (!hasLocationPermission) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            // 记录精度：如果只有 coarse，也尝试请求一次 fine（用户可以拒绝）
            if (hasLocationPermission && !hasFineLocation) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.distinct()

        if (toRequest.isNotEmpty()) permissionLauncher.launch(toRequest.toTypedArray())
    }

    // ---------- 状态栏 ----------
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
        } else onDispose { }
    }

    RecordScreen(
        viewModel = vm,
        onBack = onBack,
        hasLocationPermission = hasLocationPermission,
        hasFineLocation = hasFineLocation,
        hasNotificationPermission = hasNotificationPermission,
        requestPermissions = {
            val toRequest = buildList {
                if (!checkLocationPermission()) {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                } else if (!checkFineLocation()) {
                    // 已有大概位置，但要记录需要精确
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                if (Build.VERSION.SDK_INT >= 33 && !checkNotificationPermission()) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.distinct()

            if (toRequest.isNotEmpty()) permissionLauncher.launch(toRequest.toTypedArray())
        }
    )
}

@Composable
fun RecordScreen(
    viewModel: RecordingViewModel,
    onBack: () -> Unit,
    hasLocationPermission: Boolean,   // coarse 或 fine（给地图蓝点）
    hasFineLocation: Boolean,         // fine（给记录）
    hasNotificationPermission: Boolean,
    requestPermissions: () -> Unit
) {
    val durationText = viewModel.durationText
    val distanceKm = viewModel.distanceKm
    val maxSpeedKmh = viewModel.maxSpeedKmh
    val verticalDropM = viewModel.verticalDropM
    val isRecording = viewModel.isRecording
    val mode = viewModel.motionMode

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ 地图蓝点：只要有 coarse/fine 任一权限就启用
        RecordingMapView(
            modifier = Modifier.fillMaxSize(),
            hasLocationPermission = hasLocationPermission
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color(0x66000000), RoundedCornerShape(999.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }

        MotionModeBadge(
            isRecording = isRecording,
            mode = mode,
            hasLocationPermission = hasLocationPermission,
            hasFineLocation = hasFineLocation,
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xE6050505),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.AccessTime, null, tint = Color(0xFF8C8C8C))
                    Spacer(Modifier.height(8.dp))
                    Text("本次滑行时间", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8C8C8C))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = durationText,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "滑行里程",
                        value = String.format("%.1f km", distanceKm),
                        icon = Icons.Filled.TrendingUp
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "最高速度",
                        value = String.format("%.0f km/h", maxSpeedKmh),
                        icon = Icons.Filled.Speed
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "累计落差",
                        value = "$verticalDropM m",
                        icon = Icons.Outlined.Landscape
                    )

                    val cardColor = if (isRecording) Color(0xFFC6FF3F) else Color.White
                    val contentColor = Color.Black

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                            .background(cardColor, shape = RoundedCornerShape(24.dp))
                            .clickable {
                                // ✅ 成熟做法：
                                // 地图蓝点：coarse/fine 都行
                                // 记录：必须 fine
                                if (!hasLocationPermission) {
                                    requestPermissions()
                                    return@clickable
                                }
                                if (!hasFineLocation) {
                                    requestPermissions()
                                    return@clickable
                                }
                                if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission) {
                                    requestPermissions()
                                    return@clickable
                                }
                                viewModel.onToggleRecording()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
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
}

@Composable
private fun RecordingMapView(
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean
) {
    // 如果没有权限，显示黑色占位
    if (!hasLocationPermission) {
        Box(
            modifier = modifier.background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Text("需要定位权限以显示地图", color = Color.Gray)
        }
        return
    }

    // 有权限，显示真正的地图
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                // ✅ 加载你提供的自定义样式
                mapboxMap.loadStyle("mapbox://styles/gosnow/cmikjh06p00ys01s68fmy9nor") {
                    // 启用定位插件（显示蓝点）
                    location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                    }

                    // 启用视口插件（跟随用户位置）
                    viewport.transitionTo(
                        targetState = viewport.makeFollowPuckViewportState(),
                        transition = viewport.makeImmediateViewportTransition()
                    )
                }
            }
        }
    )
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = modifier
            .height(88.dp)
            .background(color = Color(0xFF0F0F0F), shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
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

@Composable
private fun MotionModeBadge(
    isRecording: Boolean,
    mode: MotionMode,
    hasLocationPermission: Boolean,
    hasFineLocation: Boolean,
    modifier: Modifier = Modifier
) {
    val text = when {
        !hasLocationPermission -> "无定位权限"
        hasLocationPermission && !hasFineLocation -> "仅大概定位"
        !isRecording -> "未开始"
        mode == MotionMode.LIFT -> "缆车中"
        mode == MotionMode.IDLE -> "静止中"
        else -> "滑行中"
    }

    val bg = when {
        !hasLocationPermission -> Color(0x99FF3B30)
        hasLocationPermission && !hasFineLocation -> Color(0x99007AFF)
        !isRecording -> Color(0x66000000)
        mode == MotionMode.LIFT -> Color(0x99007AFF)
        mode == MotionMode.IDLE -> Color(0x99FF9500)
        else -> Color(0x9900C853)
    }

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}
