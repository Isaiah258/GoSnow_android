package com.gosnow.app.ui.snowcircle.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.gosnow.app.ui.snowcircle.model.NotificationItem
import com.gosnow.app.ui.snowcircle.model.NotificationType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知") },
                actions = {
                    TextButton(onClick = { viewModel.markAllRead() }) { Text("全部标为已读") }
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
                    TextButton(onClick = { viewModel.refresh() }) { Text("重试") }
                }
            }

            uiState.notifications.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无通知")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(notificationText(item), style = MaterialTheme.typography.bodyLarge)
            Text(item.createdAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!item.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = MaterialTheme.colorScheme.error, shape = CircleShape)
            )
        }
    }
}

private fun notificationText(item: NotificationItem): String = when (item.type) {
    NotificationType.LIKE_POST -> "${item.actor.displayName} 赞了你的帖子"
    NotificationType.LIKE_COMMENT -> "${item.actor.displayName} 赞了你的评论"
    NotificationType.COMMENT_POST -> "${item.actor.displayName} 评论了你的帖子"
    NotificationType.REPLY_COMMENT -> "${item.actor.displayName} 回复了你的评论"
}
