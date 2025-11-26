package com.gosnow.app.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gosnow.app.ui.discover.DiscoverScreen
import com.gosnow.app.ui.feed.FeedScreen
import com.gosnow.app.ui.home.HomeScreen
import com.gosnow.app.ui.lostfound.LostAndFoundScreen
import com.gosnow.app.ui.profile.ProfileScreen

const val PROFILE_ROUTE = "profile"
const val LOST_AND_FOUND_ROUTE = "lost_and_found"

@Composable
fun GoSnowApp() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Feed,
        BottomNavItem.Discover
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onProfileClick = { navController.navigate(PROFILE_ROUTE) },
                    onNavigateToDiscoverLost = { navController.navigate(LOST_AND_FOUND_ROUTE) }
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
            composable(PROFILE_ROUTE) { ProfileScreen() }
            composable(LOST_AND_FOUND_ROUTE) { LostAndFoundScreen() }
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
