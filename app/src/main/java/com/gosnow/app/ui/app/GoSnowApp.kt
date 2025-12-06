package com.gosnow.app.ui.app

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gosnow.app.ui.discover.CarpoolPublishScreen
import com.gosnow.app.ui.discover.CarpoolScreen
import com.gosnow.app.ui.discover.DiscoverScreen
import com.gosnow.app.ui.discover.LostAndFoundPublishScreen
import com.gosnow.app.ui.discover.LostAndFoundScreen
import com.gosnow.app.ui.discover.MyCarpoolScreen
import com.gosnow.app.ui.discover.MyRoommateScreen
import com.gosnow.app.ui.discover.RoommatePublishScreen
import com.gosnow.app.ui.discover.RoommateScreen
import com.gosnow.app.ui.feed.FeedScreen
import com.gosnow.app.ui.home.BottomNavItem
import com.gosnow.app.ui.home.BottomNavigationBar
import com.gosnow.app.ui.home.HomeScreen
import com.gosnow.app.ui.login.LoginViewModel
import com.gosnow.app.ui.login.PhoneLoginScreen
import com.gosnow.app.ui.login.TermsScreen
import com.gosnow.app.ui.login.WelcomeAuthIntroScreen
import com.gosnow.app.ui.profile.ProfileScreen
import com.gosnow.app.ui.record.RecordRoute
import com.gosnow.app.ui.welcome.WelcomeFlowScreen

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

@Composable
fun GoSnowApp() {
    val authNavController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gosnow_prefs", Context.MODE_PRIVATE) }
    var hasSeenWelcome by remember { mutableStateOf(prefs.getBoolean("has_seen_welcome_v1", false)) }

    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.provideFactory(context))
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn, hasSeenWelcome) {
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
    }

    NavHost(
        navController = authNavController,
        startDestination = WELCOME_AUTH_ROUTE
    ) {
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
                    loginViewModel.logout()
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
                        val selected = currentDestination.isRouteInHierarchy(item.route)
                        if (!selected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Record.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 记录首页
            composable(BottomNavItem.Record.route) {
                HomeScreen(
                    onStartRecording = { navController.navigate(RECORD_ROUTE) },
                    onFeatureClick = { /* 记录页里的功能入口，后面再补 */ },
                    onBottomNavSelected = { item ->
                        if (item.route != BottomNavItem.Record.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    currentRoute = BottomNavItem.Record.route
                )
            }

            // 雪圈
            composable(BottomNavItem.Community.route) {
                FeedScreen(
                    onCreatePostClick = {
                        // TODO: 雪圈发帖导航
                    }
                )
            }

            // 发现首页
            composable(BottomNavItem.Discover.route) {
                DiscoverScreen(
                    onLostAndFoundClick = { navController.navigate(LOST_AND_FOUND_ROUTE) },
                    onCarpoolClick = { navController.navigate(CARPOOL_ROUTE) },
                    onRoommateClick = { navController.navigate(ROOMMATE_ROUTE) },

                )
            }

            // 个人中心
            composable(PROFILE_ROUTE) {
                ProfileScreen(onLogout = onLogout)
            }

            // —— 失物招领 —— //
            composable(LOST_AND_FOUND_ROUTE) {
                LostAndFoundScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublishClick = { navController.navigate(LOST_AND_FOUND_PUBLISH_ROUTE) }
                )
            }
            composable(LOST_AND_FOUND_PUBLISH_ROUTE) {
                LostAndFoundPublishScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublished = {
                        // 关闭发布页，回到列表
                        navController.popBackStack()
                    }
                )
            }

            // —— 顺风车 —— //
            composable(CARPOOL_ROUTE) {
                CarpoolScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublishClick = { navController.navigate(CARPOOL_PUBLISH_ROUTE) },
                    onMyCarpoolClick = { navController.navigate(MY_CARPOOL_ROUTE) }
                )
            }
            composable(CARPOOL_PUBLISH_ROUTE) {
                CarpoolPublishScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublished = {
                        navController.popBackStack()
                    }
                )
            }
            composable(MY_CARPOOL_ROUTE) {
                MyCarpoolScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // —— 拼房合租 —— //
            composable(ROOMMATE_ROUTE) {
                RoommateScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublishClick = { navController.navigate(ROOMMATE_PUBLISH_ROUTE) },
                    onMyRoommateClick = { navController.navigate(MY_ROOMMATE_ROUTE) }
                )
            }
            composable(ROOMMATE_PUBLISH_ROUTE) {
                RoommatePublishScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublished = {
                        navController.popBackStack()
                    }
                )
            }
            composable(MY_ROOMMATE_ROUTE) {
                MyRoommateScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 全屏录制页
            composable(RECORD_ROUTE) {
                RecordRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
