package com.gosnow.app.ui.snowcircle.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gosnow.app.ui.snowcircle.model.Post
import com.gosnow.app.ui.snowcircle.model.User

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onImageClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        PostHeader(post)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        if (post.imageUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ImageGrid(urls = post.imageUrls, onImageClick = onImageClick)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            post.resortName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "赞",
                        tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(text = "${post.likeCount}")
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onCommentClick) {
                    Icon(imageVector = Icons.Outlined.Comment, contentDescription = "评论")
                }
                Text(text = "${post.commentCount}")
            }
        }
    }
}

@Composable
private fun PostHeader(post: Post) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.resortName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = post.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val expanded = remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { expanded.value = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                DropdownMenuItem(text = { Text("举报") }, onClick = { expanded.value = false })
                if (post.canDelete) {
                    DropdownMenuItem(text = { Text("删除") }, onClick = { expanded.value = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGrid(urls: List<String>, onImageClick: (Int) -> Unit) {
    when (urls.size) {
        1 -> SingleImage(urls[0], onImageClick)
        2 -> TwoImages(urls, onImageClick)
        3 -> ThreeImages(urls, onImageClick)
        else -> GridImages(urls, onImageClick)
    }
}

@Composable
private fun SingleImage(url: String, onClick: (Int) -> Unit) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(0) }
    )
}

@Composable
private fun TwoImages(urls: List<String>, onClick: (Int) -> Unit) {
    Row(modifier = Modifier.height(160.dp)) {
        urls.take(2).forEachIndexed { index, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick(index) }
            )
            if (index == 0) Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun ThreeImages(urls: List<String>, onClick: (Int) -> Unit) {
    Row(modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        AsyncImage(
            model = urls[0],
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick(0) }
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = urls[1],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick(1) }
            )
            AsyncImage(
                model = urls[2],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick(2) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridImages(urls: List<String>, onClick: (Int) -> Unit) {
    val display = urls.take(4)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(display) { index, url ->
            Box {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick(index) }
                )
                if (index == display.lastIndex && urls.size > 4) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+${urls.size - 4}", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewPostCard() {
    PostCard(
        post = Post(
            id = "1",
            author = User("1", "Ada", null),
            resortName = "Niseko",
            createdAt = "2h ago",
            content = "Bluebird day!",
            imageUrls = listOf("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee"),
            likeCount = 10,
            commentCount = 5,
            isLikedByMe = false
        ),
        onClick = {},
        onLikeClick = {},
        onCommentClick = {},
        onImageClick = {},
    )
}
