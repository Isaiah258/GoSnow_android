package com.gosnow.app.ui.record

import android.Manifest
import android.app.Activity
import android.content.Context
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosnow.app.recording.RecordingViewModel
import com.gosnow.app.recording.location.SystemLocationService
import com.gosnow.app.recording.metrics.BasicMetricsComputer
import com.gosnow.app.ui.record.storage.FileSessionStore
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.location

@Composable
fun RecordRoute(
    onBack: () -> Unit
) {
    val view = LocalView.current
    val activity = view.context as? Activity
    val window = activity?.window
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }


    fun checkPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }


    // 1. 状态栏 / 导航栏改成黑色
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

    // 2. 运行时请求定位权限
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // ✅ 授权回调后立刻刷新 state，让 MapView 重新启用蓝点
        hasLocationPermission = checkPermission()
    }

    LaunchedEffect(Unit) {
        hasLocationPermission = checkPermission()
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 3. 把之前那一整套 recording 体系接进来
    val locationService = remember { SystemLocationService(context) }
    val metrics = remember { BasicMetricsComputer() }
    val sessionStore = remember { FileSessionStore(context) }
    val recorder = remember { BasicSessionRecorder(locationService, metrics) }

    val viewModel: RecordingViewModel = viewModel(
        factory = RecordingViewModel.provideFactory(
            recorder = recorder,
            store = sessionStore
        )
    )

    RecordScreen(
        viewModel = viewModel,
        onBack = onBack,
        hasLocationPermission = hasLocationPermission
    )
}

@Composable
fun RecordScreen(
    viewModel: RecordingViewModel,
    onBack: () -> Unit ,
    hasLocationPermission: Boolean
) {
    val durationText = viewModel.durationText
    val distanceKm = viewModel.distanceKm
    val maxSpeedKmh = viewModel.maxSpeedKmh
    val verticalDropM = viewModel.verticalDropM
    val isRecording = viewModel.isRecording

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景 Mapbox 地图
        RecordingMapView(
            modifier = Modifier.fillMaxSize(),
            hasLocationPermission = hasLocationPermission
        )

        // 顶部返回按钮
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

        // 底部半透明控制面板
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xE6050505), // 带一点透明度
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // 时间
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
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 第一行：里程 + 最高速度
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

                Spacer(modifier = Modifier.height(12.dp))

                // 第二行：累计落差 + 开始/结束按钮（四个卡片尺寸一致）
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
                            .clickable { viewModel.onToggleRecording() }
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
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                val mapboxMap = getMapboxMap()
                val locationPlugin = location

                // 先加 listener（后授权也能生效）
                locationPlugin.addOnIndicatorPositionChangedListener { point ->
                    // 只更新 center，不强制把 zoom 锁死在很近
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .build()
                    )
                }

                mapboxMap.loadStyleUri(
                    "mapbox://styles/gosnow/cmikjh06p00ys01s68fmy9nor"
                ) {
                    // ✅ 1) 先给一个更合理的默认 zoom（别 17.5 起步）
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(138.5, 36.7))
                            .zoom(14.5) // 14~15 更像“看雪场”，17+ 是“贴脸看一小块”
                            .build()
                    )

                    // ✅ 2) 授权后再开启蓝点
                    locationPlugin.updateSettings {
                        enabled = hasLocationPermission
                        pulsingEnabled = hasLocationPermission
                    }

                    // ✅ 3) 如果有定位，跟随到当前位置，但别每次都强制 zoom=17.5
                    if (hasLocationPermission) {
                        locationPlugin.addOnIndicatorPositionChangedListener { point ->
                            mapboxMap.setCamera(
                                CameraOptions.Builder()
                                    .center(point)
                                    // 不写 zoom，就只更新 center（用户不会被“锁死在超近视野”）
                                    .build()
                            )
                        }
                    }
                }
            }
        },
        update = { mapView ->
            // Compose state 改变会走这里：权限从 false -> true 时立刻开蓝点
            mapView.location.updateSettings {
                enabled = hasLocationPermission
                pulsingEnabled = hasLocationPermission
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
