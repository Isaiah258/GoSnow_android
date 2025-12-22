package com.gosnow.app.ui.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gosnow.app.datasupabase.CurrentUserStore
import com.gosnow.app.ui.discover.CarpoolPublishScreen
import com.gosnow.app.ui.discover.CarpoolScreen
import com.gosnow.app.ui.discover.DiscoverScreen
import com.gosnow.app.ui.discover.LostAndFoundPublishScreen
import com.gosnow.app.ui.discover.LostAndFoundScreen
import com.gosnow.app.ui.discover.MyCarpoolScreen
import com.gosnow.app.ui.discover.MyLostAndFoundScreen
import com.gosnow.app.ui.discover.MyRoommateScreen
import com.gosnow.app.ui.discover.RoommatePublishScreen
import com.gosnow.app.ui.discover.RoommateScreen
import com.gosnow.app.ui.home.BottomNavItem
import com.gosnow.app.ui.home.HomeScreen
import com.gosnow.app.ui.login.AuthViewModel
import com.gosnow.app.ui.login.PhoneLoginScreen
import com.gosnow.app.ui.login.TermsScreen
import com.gosnow.app.ui.login.WelcomeAuthIntroScreen
import com.gosnow.app.ui.record.RecordRoute
import com.gosnow.app.ui.settings.AboutScreen
import com.gosnow.app.ui.settings.AccountPrivacyScreen
import com.gosnow.app.ui.settings.EditProfileScreen
import com.gosnow.app.ui.settings.FeedbackScreen
import com.gosnow.app.ui.settings.ROUTE_ABOUT
import com.gosnow.app.ui.settings.ROUTE_ACCOUNT_PRIVACY
import com.gosnow.app.ui.settings.ROUTE_EDIT_PROFILE
import com.gosnow.app.ui.settings.ROUTE_FEEDBACK
import com.gosnow.app.ui.settings.SettingsScreen
import com.gosnow.app.ui.snowcircle.ui.SnowApp
import com.gosnow.app.ui.stats.StatsScreen
import com.gosnow.app.ui.update.UpdateNoticeDialog
import com.gosnow.app.ui.update.UpdateViewModel
import com.gosnow.app.ui.welcome.WelcomeFlowScreen

// 常量定义
private const val WELCOME_AUTH_ROUTE = "welcome_auth"
private const val PHONE_LOGIN_ROUTE = "phone_login"
private const val TERMS_ROUTE = "terms"
private const val MAIN_ROUTE = "main"

const val PROFILE_ROUTE = "profile"
const val LOST_AND_FOUND_ROUTE = "lost_and_found"
const val LOST_AND_FOUND_PUBLISH_ROUTE = "lost_and_found_publish"
const val CARPOOL_ROUTE = "carpool"
const val CARPOOL_PUBLISH_ROUTE = "carpool_publish"
const val MY_CARPOOL_ROUTE = "my_carpool"
const val ROOMMATE_ROUTE = "roommate"
const val ROOMMATE_PUBLISH_ROUTE = "roommate_publish"
const val MY_ROOMMATE_ROUTE = "my_roommate"
const val RECORD_ROUTE = "record"
const val WELCOME_FLOW_ROUTE = "welcome_flow"
const val LOST_AND_FOUND_MY_ROUTE = "lost_and_found_my"
const val STATS_ROUTE = "stats"

@Composable
fun GoSnowApp() {
    val authNavController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gosnow_prefs", Context.MODE_PRIVATE) }
    var hasSeenWelcome by remember { mutableStateOf(prefs.getBoolean("has_seen_welcome_v1", false)) }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.provideFactory(context))
    val uiState by authViewModel.uiState.collectAsState()

    val updateViewModel: UpdateViewModel = viewModel()
    val updateNotice by updateViewModel.updateNotice.collectAsState()

    LaunchedEffect(uiState.isLoggedIn, hasSeenWelcome) {
        // 如果还在检查 Session，不要执行任何跳转逻辑，防止乱跳
        if (uiState.isCheckingSession) return@LaunchedEffect

        val targetRoute = when {
            !uiState.isLoggedIn -> WELCOME_AUTH_ROUTE
            !hasSeenWelcome -> WELCOME_FLOW_ROUTE
            else -> MAIN_ROUTE
        }

        val currentRoute = authNavController.currentDestination?.route
        if (currentRoute != targetRoute) {
            authNavController.navigate(targetRoute) {
                popUpTo(authNavController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

        if (uiState.isLoggedIn) {
            updateViewModel.checkForUpdates()
        }
    }

    if (updateNotice != null) {
        UpdateNoticeDialog(
            notice = updateNotice!!,
            onDismiss = { updateViewModel.dismissUpdate() }
        )
    }

    // ✅ 修复启动闪烁：使用全屏 Loading 遮罩
    // 只有当 isCheckingSession 为 false 时，才显示 NavHost
    // 这样避免了 NavHost 先渲染登录页(默认页)再跳转主页造成的闪烁
    if (uiState.isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), // 使用黑色背景，体验更像启动页
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        // ✅ 动态设置 startDestination
        // 这样 NavHost 初始化时就会直接显示正确的页面，而不是先显示 Login 再跳转
        val startDest = when {
            !uiState.isLoggedIn -> WELCOME_AUTH_ROUTE
            !hasSeenWelcome -> WELCOME_FLOW_ROUTE
            else -> MAIN_ROUTE
        }

        NavHost(
            navController = authNavController,
            startDestination = startDest
        ) {
            composable(WELCOME_AUTH_ROUTE) {
                WelcomeAuthIntroScreen(
                    isCheckingSession = false, // 这里肯定是 false 了
                    onStartPhoneLogin = { authNavController.navigate(PHONE_LOGIN_ROUTE) },
                    onTermsClick = { authNavController.navigate(TERMS_ROUTE) }
                )
            }
            composable(PHONE_LOGIN_ROUTE) {
                PhoneLoginScreen(
                    uiState = uiState,
                    onPhoneChange = authViewModel::onPhoneChange,
                    onVerificationCodeChange = authViewModel::onVerificationCodeChange,
                    onSendCode = authViewModel::sendCode,
                    onLoginClick = authViewModel::verifyCodeAndLogin,
                    onBackClick = { authNavController.popBackStack() },
                    onTermsClick = { authNavController.navigate(TERMS_ROUTE) }
                )
            }
            composable(TERMS_ROUTE) {
                TermsScreen(onBackClick = { authNavController.popBackStack() })
            }
            composable(WELCOME_FLOW_ROUTE) {
                WelcomeFlowScreen(
                    onFinished = {
                        hasSeenWelcome = true
                        prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()
                        authNavController.navigate(MAIN_ROUTE) {
                            popUpTo(authNavController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(MAIN_ROUTE) {
                GoSnowMainApp(
                    onLogout = {
                        authViewModel.logout()
                        hasSeenWelcome = prefs.getBoolean("has_seen_welcome_v1", false)
                        authNavController.navigate(WELCOME_AUTH_ROUTE) {
                            popUpTo(authNavController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GoSnowMainApp(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Record,
        BottomNavItem.Community,
        BottomNavItem.Discover
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val shouldShowBottomBar = currentRoute != RECORD_ROUTE

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    items = items,
                    currentRoute = currentRoute.orEmpty(),
                    onItemSelected = { item ->
                        // ✅ 修复导航逻辑：
                        // 如果点击的是“记录”（Home），我们需要特殊处理。
                        // 如果当前不在“记录”的根页面（例如在 Settings/Profile 页面），
                        // 我们需要【重置】状态，而不是【恢复】状态。
                        // 恢复状态(restoreState=true)会导致把 Settings 页面又带回来，看起来像没跳转。

                        val isSelectingRecord = item.route == BottomNavItem.Record.route
                        val isCurrentlyAtRecordRoot = currentRoute == BottomNavItem.Record.route

                        // 逻辑：如果点击的是记录，且当前不在记录首页 -> 强制不恢复状态（即重置回首页）
                        // 其他情况（点击其他 Tab，或者在首页点首页） -> 保持默认行为（恢复状态）
                        val shouldRestoreState = !(isSelectingRecord && !isCurrentlyAtRecordRoot)

                        navController.navigate(item.route) {
                            // 弹出到图谱的起始点
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = shouldRestoreState
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Record.route,
            modifier = Modifier // 移除全局 padding
        ) {
            // 1. 记录首页
            composable(BottomNavItem.Record.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    HomeScreen(
                        onStartRecording = { navController.navigate(RECORD_ROUTE) },
                        onFeatureClick = { featureTitle ->
                            if (featureTitle == "滑行数据") {
                                navController.navigate(STATS_ROUTE)
                            }
                        },
                        onAvatarClick = { navController.navigate(PROFILE_ROUTE) },
                        onBottomNavSelected = { /* unused */ },
                        currentRoute = BottomNavItem.Record.route
                    )
                }
            }

            // 2. 统计页
            composable(STATS_ROUTE) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    StatsScreen()
                }
            }

            // 3. 雪圈 - 仅底部 Padding
            composable(BottomNavItem.Community.route) {
                Box(
                    modifier = Modifier.padding(
                        top = 0.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    )
                ) {
                    SnowApp()
                }
            }

            // 4. 发现 - 仅底部 Padding
            composable(BottomNavItem.Discover.route) {
                Box(
                    modifier = Modifier.padding(
                        top = 0.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    )
                ) {
                    DiscoverScreen(
                        onLostAndFoundClick = { navController.navigate(LOST_AND_FOUND_ROUTE) },
                        onCarpoolClick = { navController.navigate(CARPOOL_ROUTE) },
                        onRoommateClick = { navController.navigate(ROOMMATE_ROUTE) },
                    )
                }
            }

            // 5. 设置主页 (Profile) - 仅底部 Padding
            composable(PROFILE_ROUTE) {
                Box(
                    modifier = Modifier.padding(
                        top = 0.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    )
                ) {
                    SettingsScreen(
                        onBackClick = { navController.popBackStack() },
                        onEditProfileClick = { navController.navigate(ROUTE_EDIT_PROFILE) },
                        onFeedbackClick = { navController.navigate(ROUTE_FEEDBACK) },
                        onAccountPrivacyClick = { navController.navigate(ROUTE_ACCOUNT_PRIVACY) },
                        onAboutClick = { navController.navigate(ROUTE_ABOUT) },
                        onLogoutClick = { onLogout() }
                    )
                }
            }

            // 6. 全屏录制页
            composable(RECORD_ROUTE) {
                RecordRoute(onBack = { navController.popBackStack() })
            }

            // 7. 条款页
            composable(TERMS_ROUTE) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    TermsScreen(onBackClick = { navController.popBackStack() })
                }
            }

            // --- 其他子页面 ---

            composable(ROUTE_ACCOUNT_PRIVACY) {
                val context = LocalContext.current
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    AccountPrivacyScreen(
                        onBackClick = { navController.popBackStack() },
                        onOpenSystemSettingsClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            composable(ROUTE_FEEDBACK) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    FeedbackScreen(onBackClick = { navController.popBackStack() })
                }
            }

            composable(ROUTE_ABOUT) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    AboutScreen(
                        onBackClick = { navController.popBackStack() },
                        onCommunityGuidelinesClick = { navController.navigate(TERMS_ROUTE) },
                        onPrivacyPolicyClick = { navController.navigate(TERMS_ROUTE) }
                    )
                }
            }

            composable(ROUTE_EDIT_PROFILE) {
                val currentProfile by CurrentUserStore.profile.collectAsState()
                val currentName = currentProfile?.userName ?: "雪友"
                val avatarUrl = currentProfile?.avatarUrl

                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    EditProfileScreen(
                        currentName = currentName,
                        avatarUrl = avatarUrl,
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { navController.popBackStack() }
                    )
                }
            }

            composable(LOST_AND_FOUND_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    LostAndFoundScreen(
                        onBackClick = { navController.popBackStack() },
                        onPublishClick = { navController.navigate(LOST_AND_FOUND_PUBLISH_ROUTE) },
                        onMyLostAndFoundClick = { navController.navigate(LOST_AND_FOUND_MY_ROUTE) }
                    )
                }
            }
            composable(LOST_AND_FOUND_PUBLISH_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    LostAndFoundPublishScreen(
                        onBackClick = { navController.popBackStack() },
                        onPublished = { navController.popBackStack() }
                    )
                }
            }
            composable(LOST_AND_FOUND_MY_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    MyLostAndFoundScreen(onBackClick = { navController.popBackStack() })
                }
            }

            composable(CARPOOL_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    CarpoolScreen(
                        onBackClick = { navController.popBackStack() },
                        onPublishClick = { navController.navigate(CARPOOL_PUBLISH_ROUTE) },
                        onMyCarpoolClick = { navController.navigate(MY_CARPOOL_ROUTE) }
                    )
                }
            }
            composable(CARPOOL_PUBLISH_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    CarpoolPublishScreen(
                        onBackClick = { navController.popBackStack() },
                        onPublished = { navController.popBackStack() }
                    )
                }
            }
            composable(MY_CARPOOL_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    MyCarpoolScreen(onBackClick = { navController.popBackStack() })
                }
            }

            composable(ROOMMATE_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    RoommateScreen(
                        onBackClick = { navController.popBackStack() },
                        onPublishClick = { navController.navigate(ROOMMATE_PUBLISH_ROUTE) },
                        onMyRoommateClick = { navController.navigate(MY_ROOMMATE_ROUTE) }
                    )
                }
            }
            composable(ROOMMATE_PUBLISH_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    RoommatePublishScreen(
                        onBackClick = { navController.popBackStack() },
                        onPublished = { navController.popBackStack() }
                    )
                }
            }
            composable(MY_ROOMMATE_ROUTE) {
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    MyRoommateScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemSelected: (BottomNavItem) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}