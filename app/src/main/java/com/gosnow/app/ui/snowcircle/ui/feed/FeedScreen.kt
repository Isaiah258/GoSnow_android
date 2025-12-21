package com.gosnow.app.ui.snowcircle.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox // ✅ 现在可以使用了 (需 Material3 1.3.0+)
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gosnow.app.ui.snowcircle.ui.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 监听自动刷新信号
    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    val refreshTriggerState = savedStateHandle?.getStateFlow("refresh_feed", false)
    val refreshTrigger by refreshTriggerState?.collectAsState() ?: mutableStateOf(false)

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.refresh()
            savedStateHandle?.set("refresh_feed", false)
        }
    }

    // 监听用户提示消息 (删除/举报反馈)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.messageShown()
        }
    }

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
                            Icon(Icons.Filled.Notifications, contentDescription = "通知")
                        }
                        if (uiState.hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("my_posts") }) {
                        Icon(Icons.Outlined.Article, contentDescription = "我的帖子")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("compose_post") },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "发布")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // ✅ 使用 PullToRefreshBox 实现原生下拉刷新
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!uiState.isLoading && uiState.posts.isEmpty() && uiState.errorMessage == null) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val msg = if (uiState.selectedResort == null) "暂无帖子" else "该雪场暂无帖子"
                    Text(msg, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                ) {
                    // 搜索栏
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SearchBar(
                            value = uiState.query,
                            onValueChange = viewModel::onQueryChange,
                            onClear = { viewModel.onQueryChange("") }
                        )
                        if (uiState.query.isNotBlank() && uiState.selectedResort == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 帖子列表
                    itemsIndexed(uiState.posts.take(uiState.visibleCount), key = { _, item -> item.id }) { index, post ->
                        PostCard(
                            post = post,
                            onClick = { navController.navigate("post_detail/${post.id}") },
                            onLikeClick = { viewModel.onToggleLike(post.id) },
                            onCommentClick = { navController.navigate("post_detail/${post.id}") },
                            onImageClick = { idx -> navController.navigate("image_viewer/${post.id}/$idx") },
                            // ✅ 连接删除和举报逻辑
                            onDeleteClick = { viewModel.onDeletePost(post.id) },
                            onReportClick = { viewModel.onReportPost(post.id) }
                        )
                        if (index < uiState.posts.take(uiState.visibleCount).lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }

                    // 加载更多
                    item {
                        if (uiState.visibleCount < uiState.posts.size) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { viewModel.onLoadMore() }) { Text("加载更多") }
                            }
                        }
                    }
                }
            }

            // 错误重试按钮
            if (uiState.errorMessage != null && uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "加载失败")
                        TextButton(onClick = { viewModel.refresh() }) { Text("重试") }
                    }
                }
            }
        }
    }
}

// SearchBar & SelectedResortCard 保持不变
@Composable
private fun SearchBar(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        placeholder = { Text("搜索帖子或雪场") },
        leadingIcon = { Icon(Icons.Filled.Search, null) },
        trailingIcon = { if (value.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Filled.Close, null) } },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White, focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun SelectedResortCard(resort: String, onClear: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("已选择雪场", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(resort, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onClear) { Icon(Icons.Filled.Close, "移除雪场") }
        }
    }
}