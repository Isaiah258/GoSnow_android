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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Text(
                text = post.createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

/** 灰色胶囊雪场标签 */
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
fun ImageGrid(urls: List<String>, onImageClick: (Int) -> Unit) {
    when (urls.size) {
        1 -> SingleImage(urls[0], onImageClick)
        2 -> TwoImages(urls, onImageClick)
        3 -> ThreeImages(urls, onImageClick)
        4 -> FourImages(urls, onImageClick)
        else -> ManyImages(urls, onImageClick)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        urls.take(2).forEachIndexed { index, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick(index) }
            )
        }
    }
}

@Composable
private fun ThreeImages(urls: List<String>, onClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AsyncImage(
            model = urls[0],
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(2f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick(0) }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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

@Composable
private fun FourImages(urls: List<String>, onClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.take(2).forEachIndexed { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick(index) }
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.drop(2).take(2).forEachIndexed { index, url ->
                val realIndex = index + 2
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick(realIndex) }
                )
            }
        }
    }
}

@Composable
private fun ManyImages(urls: List<String>, onClick: (Int) -> Unit) {
    val display = urls.take(4)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            display.take(2).forEachIndexed { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick(index) }
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            display.drop(2).take(2).forEachIndexed { index, url ->
                val realIndex = index + 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick(realIndex) }
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    if (realIndex == display.lastIndex && urls.size > 4) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${urls.size - 4}",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
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
            modifier = Modifier.size(18.dp)   // ✅ 关键：视觉变小
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
