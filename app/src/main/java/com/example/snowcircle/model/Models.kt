package com.example.snowcircle.model

data class User(
    val id: String,
    val displayName: String,
    val avatarUrl: String?
)

data class Post(
    val id: String,
    val author: User,
    val resortName: String?,
    val createdAt: String,
    val content: String,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByMe: Boolean,
    val canDelete: Boolean = false
)

data class Comment(
    val id: String,
    val postId: String,
    val parentId: String?,
    val author: User,
    val createdAt: String,
    val body: String,
    val likeCount: Int,
    val isLikedByMe: Boolean,
    val canDelete: Boolean = false
)

data class NotificationItem(
    val id: Long,
    val type: NotificationType,
    val createdAt: String,
    val postId: String?,
    val commentId: String?,
    val actor: User,
    val isRead: Boolean
)

enum class NotificationType {
    LIKE_POST, LIKE_COMMENT, COMMENT_POST, REPLY_COMMENT
}
