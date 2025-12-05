package com.gosnow.app.ui.app

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gosnow.app.ui.discover.DiscoverScreen
import com.gosnow.app.ui.feed.FeedScreen
import com.gosnow.app.ui.home.HomeScreen
import com.gosnow.app.ui.login.LoginViewModel
import com.gosnow.app.ui.login.PhoneLoginScreen
import com.gosnow.app.ui.login.TermsScreen
import com.gosnow.app.ui.login.WelcomeAuthIntroScreen
import com.gosnow.app.ui.lostfound.LostAndFoundScreen
import com.gosnow.app.ui.profile.ProfileScreen
import com.gosnow.app.ui.record.RecordRoute
import com.gosnow.app.ui.welcome.WelcomeFlowScreen

private const val WELCOME_AUTH_ROUTE = "welcome_auth"
private const val PHONE_LOGIN_ROUTE = "phone_login"
private const val TERMS_ROUTE = "terms"
private const val MAIN_ROUTE = "main"
const val PROFILE_ROUTE = "profile"
const val LOST_AND_FOUND_ROUTE = "lost_and_found"

// 记录页
const val RECORD_ROUTE = "record"

// 欢迎引导
const val WELCOME_FLOW_ROUTE = "welcome_flow"

@Composable
fun GoSnowApp() {
    val authNavController = rememberNavController()
    val context = LocalContext.current
    val loginViewModel: LoginViewModel =
        viewModel(factory = LoginViewModel.provideFactory(context))
    val uiState by loginViewModel.uiState.collectAsState()

    // 是否已经看过欢迎引导
    var hasSeenWelcome by remember { mutableStateOf(false) }
    var prefsLoaded by remember { mutableStateOf(false) }

    // 只在首次进入时，从 SharedPreferences 读取一次
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("gosnow_prefs", Context.MODE_PRIVATE)
        hasSeenWelcome = prefs.getBoolean("has_seen_welcome_v1", false)
        prefsLoaded = true
    }

    // 在偏好还没读出来前先不画 NavHost，避免闪一下
    if (!prefsLoaded) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    NavHost(
        navController = authNavController,
        startDestination = when {
            !uiState.isLoggedIn -> WELCOME_AUTH_ROUTE
            hasSeenWelcome -> MAIN_ROUTE
            else -> WELCOME_FLOW_ROUTE
        }
    ) {
        // 欢迎引导页
        composable(WELCOME_FLOW_ROUTE) {
            WelcomeFlowScreen(
                onFinished = {
                    // 标记为已看过 + 持久化
                    hasSeenWelcome = true
                    val prefs =
                        context.getSharedPreferences("gosnow_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()

                    // 进入主界面，并把欢迎页从 back stack 移除
                    authNavController.navigate(MAIN_ROUTE) {
                        popUpTo(WELCOME_FLOW_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // 登录方式选择
        composable(WELCOME_AUTH_ROUTE) {
            WelcomeAuthIntroScreen(
                isCheckingSession = uiState.isCheckingSession,
                onStartPhoneLogin = { authNavController.navigate(PHONE_LOGIN_ROUTE) },
                onTermsClick = { authNavController.navigate(TERMS_ROUTE) }
            )
        }

        composable(PHONE_LOGIN_ROUTE) {
            PhoneLoginScreen(
                uiState = uiState,
                onPhoneChange = loginViewModel::onPhoneChange,
                onVerificationCodeChange = loginViewModel::onVerificationCodeChange,
                onSendCode = loginViewModel::sendVerificationCode,
                onLoginClick = loginViewModel::verifyCodeAndLogin,
                onBackClick = { authNavController.popBackStack() },
                onTermsClick = { authNavController.navigate(TERMS_ROUTE) }
            )
        }

        composable(TERMS_ROUTE) {
            TermsScreen(onBackClick = { authNavController.popBackStack() })
        }

        composable(MAIN_ROUTE) {
            GoSnowMainApp(
                onLogout = {
                    loginViewModel.logout()
                    authNavController.navigate(WELCOME_AUTH_ROUTE) {
                        popUpTo(MAIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }

    // 监听登录状态变化：从未登录 -> 登录成功时，决定进欢迎还是直接进首页
    LaunchedEffect(uiState.isLoggedIn, hasSeenWelcome) {
        if (uiState.isLoggedIn) {
            if (!hasSeenWelcome) {
                authNavController.navigate(WELCOME_FLOW_ROUTE) {
                    popUpTo(WELCOME_AUTH_ROUTE) { inclusive = true }
                }
            } else {
                authNavController.navigate(MAIN_ROUTE) {
                    popUpTo(WELCOME_AUTH_ROUTE) { inclusive = true }
                }
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
        BottomNavItem.Home,
        BottomNavItem.Feed,
        BottomNavItem.Discover
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isRecordScreen = currentRoute == RECORD_ROUTE

    Scaffold(
        // 记录页用黑色背景，其它页面用主题背景色
        containerColor = if (isRecordScreen) Color.Black
        else MaterialTheme.colorScheme.background,
        bottomBar = {
            // 记录页隐藏底部导航栏
            if (!isRecordScreen) {
                NavigationBar {
                    items.forEach { item ->
                        val selected = currentDestination.isRouteInHierarchy(item.route)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onProfileClick = { navController.navigate(PROFILE_ROUTE) },
                    onNavigateToDiscoverLost = { navController.navigate(LOST_AND_FOUND_ROUTE) },
                    onNavigateToRecord = { navController.navigate(RECORD_ROUTE) }
                )
            }
            composable(BottomNavItem.Feed.route) {
                FeedScreen(
                    onCreatePostClick = {
                        // TODO: 接入雪圈发帖导航逻辑
                    }
                )
            }
            composable(BottomNavItem.Discover.route) {
                DiscoverScreen(
                    onLostAndFoundClick = { navController.navigate(LOST_AND_FOUND_ROUTE) },
                    onFeatureClick = {
                        // TODO: 根据 iOS 发现页入口补充导航
                    }
                )
            }
            composable(PROFILE_ROUTE) { ProfileScreen(onLogout = onLogout) }
            composable(LOST_AND_FOUND_ROUTE) { LostAndFoundScreen() }

            // 记录页
            composable(RECORD_ROUTE) {
                RecordRoute(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "首页", Icons.Filled.Home)
    data object Feed : BottomNavItem("feed", "雪圈", Icons.Filled.Public)
    data object Discover : BottomNavItem("discover", "发现", Icons.Filled.Explore)
}

private fun NavDestination?.isRouteInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
