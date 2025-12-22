package com.gosnow.app.ui.snowcircle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gosnow.app.ui.snowcircle.model.Post

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onImageClick: (index: Int) -> Unit,
    // ✅ 新增回调参数
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 将回调传给 Header
        PostHeader(
            post = post,
            onDeleteClick = onDeleteClick,
            onReportClick = onReportClick
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )

        if (post.imageUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ImageGrid(urls = post.imageUrls, onImageClick = onImageClick)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            post.resortName?.let { ResortTag(name = it) }
            Spacer(modifier = Modifier.weight(1f))

            ActionPill(
                icon = if (post.isLikedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                count = post.likeCount,
                selected = post.isLikedByMe,
                onClick = onLikeClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            ActionPill(
                icon = Icons.Outlined.Comment,
                count = post.commentCount,
                onClick = onCommentClick
            )
        }
        // 分割线移到外部 List 处理，这里不加
    }
}

@Composable
private fun PostHeader(
    post: Post,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = post.author.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = post.author.displayName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = post.createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ✅ 下拉菜单逻辑
        val expanded = remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { expanded.value = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text("举报") },
                    onClick = {
                        expanded.value = false
                        onReportClick() // 触发回调
                    }
                )
                // 只有属于自己的帖子且允许删除时才显示删除选项
                if (post.canDelete) {
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded.value = false
                            onDeleteClick() // 触发回调
                        }
                    )
                }
            }
        }
    }
}

// ... 剩余的 ImageGrid, ResortTag, ActionPill 等保持不变 (你可以保留你原来的，或者确保它们存在)
@Composable
fun ResortTag(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF0F0F0))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    count: Int,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ImageGrid(urls: List<String>, onImageClick: (Int) -> Unit) {
    // 简化版实现，防止依赖丢失，你可以用你之前的复杂版
    if (urls.isEmpty()) return
    val displayUrl = urls.first()
    AsyncImage(
        model = displayUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16/9f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onImageClick(0) }
    )
}