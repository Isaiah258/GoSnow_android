package com.example.snowcircle.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snowcircle.model.NotificationItem
import com.example.snowcircle.model.NotificationType
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知 / Notifications") },
                actions = {
                    TextButton(onClick = { viewModel.markAllRead() }) { Text("Mark all read") }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.errorMessage != null -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage!!)
                    TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }
            uiState.notifications.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No notifications")
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(uiState.notifications, key = { it.id }) { item ->
                    NotificationRow(item = item, onClick = {
                        scope.launch {
                            val postId = viewModel.onNotificationTapped(item.id)
                            if (postId != null) {
                                navController.navigate("post_detail/$postId")
                            }
                        }
                    })
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Text(notificationText(item), style = MaterialTheme.typography.bodyLarge)
        Text(item.createdAt, style = MaterialTheme.typography.bodySmall)
        if (!item.isRead) {
            Text("Unread", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun notificationText(item: NotificationItem): String = when (item.type) {
    NotificationType.LIKE_POST -> "${item.actor.displayName} liked your post"
    NotificationType.LIKE_COMMENT -> "${item.actor.displayName} liked your comment"
    NotificationType.COMMENT_POST -> "${item.actor.displayName} commented on your post"
    NotificationType.REPLY_COMMENT -> "${item.actor.displayName} replied to your comment"
}
