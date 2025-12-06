package com.example.snowcircle.ui.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snowcircle.ui.components.CommentRow
import com.example.snowcircle.ui.components.ImageGrid
import com.example.snowcircle.model.Comment
import com.example.snowcircle.model.Post

@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel,
    navController: NavController
) {
    val uiState = viewModel.uiState.collectAsState()
    val replyText = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            uiState.value.replyTarget?.let { target ->
                ReplyComposerBar(
                    targetName = target.author.displayName,
                    text = replyText.value,
                    onTextChange = { replyText.value = it },
                    onSend = {
                        if (replyText.value.isNotBlank()) {
                            viewModel.onSendComment(replyText.value, target.id)
                            replyText.value = ""
                        }
                    },
                    onDismiss = { viewModel.onReplyTargetSelected(null) }
                )
            }
        }
    ) { padding ->
        when {
            uiState.value.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.value.errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.value.errorMessage!!)
                    TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }
            uiState.value.post != null -> PostDetailContent(
                post = uiState.value.post,
                comments = uiState.value.comments,
                onImageClick = { index -> navController.navigate("image_viewer/${uiState.value.post.id}/$index") },
                onLike = { viewModel.onTogglePostLike() },
                onComment = { viewModel.onReplyTargetSelected(null) },
                onReplyComment = { comment -> viewModel.onReplyTargetSelected(comment) },
                onLikeComment = { comment -> viewModel.onToggleCommentLike(comment.id) },
                onDeleteComment = { comment -> viewModel.onDeleteComment(comment.id) },
                onReport = { }
            )
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
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(post.author.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(post.createdAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(post.content, style = MaterialTheme.typography.bodyLarge)
                if (post.imageUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ImageGrid(urls = post.imageUrls, onImageClick = onImageClick)
                }
                Spacer(modifier = Modifier.height(8.dp))
                post.resortName?.let { Text("#${it}", color = MaterialTheme.colorScheme.primary) }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilledTonalButton(onClick = onLike) { Text("👍 ${post.likeCount}") }
                    FilledTonalButton(onClick = onComment) { Text("💬 ${post.commentCount}") }
                }
            }
            Divider()
            Text("评论 / Comments", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
        }
        if (comments.roots.isEmpty()) {
            item { Text("暂无评论 / No comments", modifier = Modifier.padding(16.dp)) }
        } else {
            items(comments.roots, key = { it.id }) { root ->
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    CommentRow(
                        comment = root,
                        onReply = onReplyComment,
                        onLike = onLikeComment,
                        onDelete = onDeleteComment,
                        onReport = onReport
                    )
                    val replies = comments.children[root.id].orEmpty()
                    Column(modifier = Modifier.padding(start = 32.dp)) {
                        replies.take(3).forEach { reply ->
                            CommentRow(
                                comment = reply,
                                onReply = onReplyComment,
                                onLike = onLikeComment,
                                onDelete = onDeleteComment,
                                onReport = onReport
                            )
                        }
                        if (replies.size > 3) {
                            TextButton(onClick = { /* TODO expand */ }) { Text("展开更多 / Show more") }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ReplyComposerBar(
    targetName: String,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Reply to $targetName", style = MaterialTheme.typography.bodySmall)
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Say something…") }
            )
        }
        IconButton(onClick = onSend) { Icon(Icons.Filled.Send, contentDescription = null) }
        IconButton(onClick = onDismiss) { Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel") }
    }
}
