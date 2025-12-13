package com.gosnow.app.ui.snowcircle.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResortPostRow(
    val id: String,                 // uuid
    val created_at: String,         // timestamptz -> string
    val author_id: String? = null,
    val resort_id: Long? = null,
    val title: String? = null,
    val body: String? = null,
    val rating: Int? = null,
)

@Serializable
data class UserRow(
    val id: String,
    val user_name: String,
    val avatar_url: String? = null
)

@Serializable
data class ResortRow(
    val id: Long,
    @SerialName("name_resort")
    val name: String
)

@Serializable
data class PostImageRow(
    val id: Long,
    val post_id: String,
    val user_id: String? = null,
    val url: String? = null
)

@Serializable
data class PostLikeRow(
    val post_id: String,
    val author_id: String,
)

@Serializable
data class PostCommentRow(
    val id: String,
    val post_id: String,
    val user_id: String,
    val parent_comment_id: String? = null,
    val body: String,
    val created_at: String? = null,
)

@Serializable
data class CommentLikeRow(
    val comment_id: String,
    val user_id: String
)

@Serializable
data class ResortNotificationRow(
    val id: Long,
    val recipient_user_id: String,
    val actor_user_id: String,
    val type: String,          // notification_type enum 的文本值：like_post / like_comment / comment_post / reply_comment
    val post_id: String? = null,
    val comment_id: String? = null,
    val created_at: String? = null,
    val read_at: String? = null
)

// insert payloads
@Serializable
data class ResortPostInsert(
    val resort_id: Long,
    val body: String,
    val title: String? = null,
    val rating: Int = 0
)

@Serializable
data class PostImageInsert(
    val post_id: String,
    val user_id: String,
    val url: String
)

@Serializable
data class PostCommentInsert(
    val post_id: String,
    val user_id: String,
    val parent_comment_id: String? = null,
    val body: String
)

@Serializable
data class MarkReadPatch(
    val read_at: String
)

