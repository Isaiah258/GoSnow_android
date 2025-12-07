package com.gosnow.app.ui.snowcircle.ui.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gosnow.app.ui.snowcircle.model.Post
import com.gosnow.app.ui.snowcircle.model.User
import com.gosnow.app.ui.snowcircle.ui.components.PostCard
import com.gosnow.app.ui.snowcircle.ui.theme.SnowTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("雪圈") },
                navigationIcon = {
                    Box {
                        IconButton(onClick = {
                            viewModel.onNotificationsViewed()
                            navController.navigate("notifications")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "通知"
                            )
                        }
                        if (uiState.hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("my_posts") }) {
                        Icon(Icons.Outlined.Article, contentDescription = "我的帖子")
                    }
                    IconButton(onClick = { navController.navigate("compose_post") }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "发布")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("compose_post") }) {
                Icon(Icons.Filled.Add, contentDescription = "发布")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Spacer(modifier = Modifier.height(8.dp))
            SearchBar(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                onClear = { viewModel.onQueryChange("") }
            )
            if (uiState.query.isNotBlank() && uiState.selectedResort == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.resortSuggestions.forEach { resort ->
                        AssistChip(
                            onClick = { viewModel.onResortSelected(resort) },
                            label = { Text(resort) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
            uiState.selectedResort?.let {
                SelectedResortCard(resort = it, onClear = { viewModel.onResortSelected(null) })
            }
            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                uiState.errorMessage != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "加载失败")
                        TextButton(onClick = { viewModel.refresh() }) { Text("重试") }
                    }
                }

                uiState.posts.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val msg = if (uiState.selectedResort == null) "暂无帖子" else "该雪场暂无帖子，快来分享吧"
                    Text(msg)
                }

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(uiState.posts, key = { _, item -> item.id }) { index, post ->
                        PostCard(
                            post = post,
                            onClick = { navController.navigate("post_detail/${post.id}") },
                            onLikeClick = { viewModel.onToggleLike(post.id) },
                            onCommentClick = { navController.navigate("post_detail/${post.id}") },
                            onImageClick = { imageIndex -> navController.navigate("image_viewer/${post.id}/$imageIndex") }
                        )
                        if (index < uiState.posts.lastIndex) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("搜索帖子或雪场") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) { Icon(Icons.Filled.Close, contentDescription = "清空") }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun SelectedResortCard(resort: String, onClear: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("已选择雪场", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(resort, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { /* TODO: open resort detail */ }) { Text("查看雪场详情") }
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = "移除雪场")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewFeed() {
    SnowTheme {
        val dummyPosts = listOf(
            Post("1", User("1", "Ada", null), "Niseko", "1h ago", "Powder day!", emptyList(), 3, 1, false),
            Post("2", User("2", "Ben", null), null, "3h ago", "Looking for buddies", listOf("https://picsum.photos/200"), 2, 0, false),
        )
        FeedScreen(
            viewModel = FakeFeedViewModel(dummyPosts),
            navController = rememberNavController()
        )
    }
}

private class FakeFeedViewModel(private val posts: List<Post>) : FeedViewModel(
    postRepository = object : com.gosnow.app.ui.snowcircle.data.PostRepository {
        override suspend fun getFeedPosts(resortFilter: String?) = posts
        override suspend fun getMyPosts(currentUserId: String) = emptyList<Post>()
        override suspend fun getPostById(postId: String) = posts.firstOrNull()
        override suspend fun toggleLike(postId: String, currentUserId: String) = posts.firstOrNull()
        override suspend fun deletePost(postId: String, currentUserId: String) {}
        override suspend fun createPost(content: String, resortName: String?, images: List<String>, currentUser: User) = posts.first()
    },
    notificationsRepository = object : com.gosnow.app.ui.snowcircle.data.NotificationsRepository {
        override suspend fun getNotifications(currentUserId: String) = emptyList<com.gosnow.app.ui.snowcircle.model.NotificationItem>()
        override suspend fun markAllRead(currentUserId: String) {}
        override suspend fun markRead(notificationId: Long) {}
    },
    currentUserId = "",
) {
    init {
        onQueryChange("")
        onResortSelected(null)
    }

    override fun refresh() {
        // no-op for preview
    }
}
