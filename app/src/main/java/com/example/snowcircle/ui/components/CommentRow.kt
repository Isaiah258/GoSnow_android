package com.example.snowcircle.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.snowcircle.model.Comment

@Composable
fun CommentRow(
    comment: Comment,
    onReply: (Comment) -> Unit,
    onLike: (Comment) -> Unit,
    onDelete: (Comment) -> Unit,
    onReport: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onReply(comment) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.author.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = comment.author.displayName, style = MaterialTheme.typography.titleMedium)
                val expanded = remember { mutableStateOf(false) }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded.value = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                    DropdownMenuItem(text = { Text("Report") }, onClick = {
                        expanded.value = false
                        onReport(comment)
                    })
                    if (comment.canDelete) {
                        DropdownMenuItem(text = { Text("Delete") }, onClick = {
                            expanded.value = false
                            onDelete(comment)
                        })
                    }
                }
            }
            Text(text = comment.body, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = comment.createdAt, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { onReply(comment) }) { Text("Reply") }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onLike(comment) }) {
                    Icon(
                        imageVector = if (comment.isLikedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = null,
                        tint = if (comment.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(text = "${comment.likeCount}")
            }
        }
    }
}
