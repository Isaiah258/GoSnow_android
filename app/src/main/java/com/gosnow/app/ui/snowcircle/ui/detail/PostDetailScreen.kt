package com.gosnow.app.ui.snowcircle.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gosnow.app.ui.snowcircle.model.Comment
import com.gosnow.app.ui.snowcircle.model.Post
import com.gosnow.app.ui.snowcircle.ui.components.CommentRow
import com.gosnow.app.ui.snowcircle.ui.components.ImageGrid
import com.gosnow.app.ui.snowcircle.ui.components.ResortTag
import androidx.compose.material.icons.filled.SwapVert

private enum class CommentSortMode { HOT, TIME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel,
    navController: NavController
) {
    val uiState = viewModel.uiState.collectAsState()
    val replyText = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帖子") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            ReplyComposerBar(
                isReplyToPost = uiState.value.replyTarget == null,
                targetName = uiState.value.replyTarget?.author?.displayName,
                text = replyText.value,
                onTextChange = { replyText.value = it },
                onSend = {
                    if (replyText.value.isNotBlank()) {
                        val parentId = uiState.value.replyTarget?.id
                        viewModel.onSendComment(replyText.value, parentId)
                        replyText.value = ""
                    }
                },
                focusRequester = focusRequester
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .padding(padding)
        ) {
            when {
                uiState.value.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.value.errorMessage != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.value.errorMessage!!)
                        TextButton(onClick = { viewModel.refresh() }) { Text("重试") }
                    }
                }

                uiState.value.post != null -> {
                    val post = uiState.value.post!!
                    PostDetailContent(
                        post = post,
                        comments = uiState.value.comments,
                        onImageClick = { index ->
                            navController.navigate("image_viewer/${post.id}/$index")
                        },
                        onLike = { viewModel.onTogglePostLike() },
                        onComment = {
                            viewModel.onReplyTargetSelected(null)
                            focusRequester.requestFocus()
                        },
                        onReplyComment = { comment ->
                            viewModel.onReplyTargetSelected(comment)
                            focusRequester.requestFocus()
                        },
                        onLikeComment = { comment -> viewModel.onToggleCommentLike(comment.id) },
                        onDeleteComment = { comment -> viewModel.onDeleteComment(comment.id) },
                        onReport = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    post: Post,
    comments: CommentThreadUiState,
    onImageClick: (Int) -> Unit,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onReplyComment: (Comment) -> Unit,
    onLikeComment: (Comment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onReport: (Comment) -> Unit
) {
    // ✅ 默认热度；点按钮在 热度 <-> 时间 之间切换
    var sortMode by rememberSaveable { mutableStateOf(CommentSortMode.HOT) }

    // ✅ 只影响主评论（roots），楼中楼不排序
    val sortedRoots = remember(comments, sortMode) {
        when (sortMode) {
            CommentSortMode.TIME -> {
                // 时间：保持后端/仓库返回顺序（通常就是 created_at desc）
                comments.roots
            }
            CommentSortMode.HOT -> {
                comments.roots.sortedWith(
                    compareByDescending<Comment> { c ->
                        val replyCount = comments.children[c.id]?.size ?: 0
                        // 热度：点赞 + 回复数（你也可以再加权重）
                        c.likeCount + replyCount
                    }.thenByDescending { c ->
                        comments.children[c.id]?.size ?: 0
                    }
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.author.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = post.author.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = post.createdAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(post.content, style = MaterialTheme.typography.bodyLarge)

                if (post.imageUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ImageGrid(urls = post.imageUrls, onImageClick = onImageClick)
                }

                Spacer(modifier = Modifier.height(8.dp))
                post.resortName?.let { ResortTag(name = it) }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickableNoRipple { onLike() }
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "赞",
                            tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${post.likeCount}")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickableNoRipple { onComment() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Comment,
                            contentDescription = "评论",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${post.commentCount}")
                    }
                }
            }

            Divider()

            // ✅ “评论”左侧标题 + 右侧一个排序按钮（点一下切换）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("评论", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))

                SortToggle(
                    mode = sortMode,
                    onToggle = {
                        sortMode = if (sortMode == CommentSortMode.HOT) CommentSortMode.TIME else CommentSortMode.HOT
                    }
                )
            }
        }

        if (comments.roots.isEmpty()) {
            item {
                Text(
                    "暂无评论",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sortedRoots, key = { it.id }) { root ->
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    CommentRow(
                        comment = root,
                        onReply = onReplyComment,
                        onLike = onLikeComment,
                        onDelete = onDeleteComment,
                        onReport = onReport
                    )

                    val replies = comments.children[root.id].orEmpty()

                    // ✅ 折叠楼中楼（不改变 replies 的顺序）
                    val previewCount = 2
                    var expanded by rememberSaveable(root.id) { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(start = 32.dp)) {
                        val shownReplies = if (expanded) replies else replies.take(previewCount)

                        shownReplies.forEach { reply ->
                            CommentRow(
                                comment = reply,
                                onReply = onReplyComment,
                                onLike = onLikeComment,
                                onDelete = onDeleteComment,
                                onReport = onReport
                            )
                        }

                        if (replies.size > previewCount) {
                            TextButton(
                                onClick = { expanded = !expanded },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                val hiddenCount = replies.size - previewCount
                                Text(text = if (expanded) "收起回复" else "展开更多回复（$hiddenCount）")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
@Composable
private fun SortToggle(
    mode: CommentSortMode,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SwapVert,
                contentDescription = "切换排序",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (mode == CommentSortMode.HOT) "按热度" else "按时间",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/** 比较简易的无水波 clickable，用来做点赞/评论区域 */
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            onClick = onClick
        )
    )

@Composable
private fun ReplyComposerBar(
    isReplyToPost: Boolean,
    targetName: String?,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    focusRequester: FocusRequester
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isReplyToPost) "发表评论" else "回复 $targetName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("写点什么…") },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        IconButton(
            onClick = {
                onSend()
                focusManager.clearFocus()
            }
        ) {
            Icon(Icons.Filled.Send, contentDescription = "发送")
        }
    }
}
