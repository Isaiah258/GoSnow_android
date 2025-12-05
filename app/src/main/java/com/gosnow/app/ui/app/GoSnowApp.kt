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
import com.gosnow.app.ui.discover.DiscoverScreen
import com.gosnow.app.ui.feed.FeedScreen
import com.gosnow.app.ui.home.BottomNavItem
import com.gosnow.app.ui.home.BottomNavigationBar
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
    val shouldShowBottomBar = currentRoute != RECORD_ROUTE && currentRoute != BottomNavItem.Record.route

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
            composable(BottomNavItem.Record.route) {
                HomeScreen(
                    onStartRecording = { navController.navigate(RECORD_ROUTE) },
                    onFeatureClick = { /* TODO: add navigation for features */ },
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
            composable(BottomNavItem.Community.route) {
                FeedScreen(
                    onCreatePostClick = {
                        // TODO: 入雪圈发帖导航逻辑
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
            composable(RECORD_ROUTE) { RecordRoute(onBack = { navController.popBackStack() }) }
        }
    }
}
private fun NavDestination?.isRouteInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
