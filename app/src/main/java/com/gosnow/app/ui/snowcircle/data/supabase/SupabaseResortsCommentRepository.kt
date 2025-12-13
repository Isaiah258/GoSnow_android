package com.gosnow.app.ui.snowcircle.data.supabase

import com.gosnow.app.ui.snowcircle.data.CommentRepository
import com.gosnow.app.ui.snowcircle.model.Comment
import com.gosnow.app.ui.snowcircle.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SupabaseResortsCommentRepository(
    private val supabase: SupabaseClient
) : CommentRepository {

    override suspend fun getCommentsForPost(postId: String): List<Comment> {
        val myId = supabase.auth.currentUserOrNull()?.id

        val rows = supabase.from("resorts_post_comments")
            .select(Columns.Companion.raw("id, post_id, user_id, parent_comment_id, body, created_at")) {
                filter { eq("post_id", postId) }
                order("created_at", Order.DESCENDING)
                limit(200)
            }.decodeList<PostCommentRow>()

        if (rows.isEmpty()) return emptyList()

        val userIds = rows.map { it.user_id }.distinct()
        val users = supabase.from("Users")
            .select(Columns.Companion.raw("id, user_name, avatar_url")) {
                filter { isIn("id", userIds) }
            }.decodeList<UserRow>()
            .associateBy { it.id }

        val commentIds = rows.map { it.id }
        val likes = supabase.from("resorts_comment_likes")
            .select(Columns.Companion.raw("comment_id, user_id")) {
                filter { isIn("comment_id", commentIds) }
            }.decodeList<CommentLikeRow>()

        val likeCountMap = likes.groupingBy { it.comment_id }.eachCount()
        val likedByMeSet = myId?.let { me ->
            likes.filter { it.user_id == me }.map { it.comment_id }.toSet()
        } ?: emptySet()

        return rows.map { r ->
            val u = users[r.user_id]
            val author = User(
                id = r.user_id,
                displayName = u?.user_name ?: "Unknown",
                avatarUrl = u?.avatar_url
            )
            Comment(
                id = r.id,
                postId = r.post_id,
                parentId = r.parent_comment_id,
                author = author,
                createdAt = timeAgo(r.created_at),
                body = r.body,
                likeCount = likeCountMap[r.id] ?: 0,
                isLikedByMe = likedByMeSet.contains(r.id),
                canDelete = (myId != null && r.user_id == myId)
            )
        }
    }

    override suspend fun addComment(
        postId: String,
        body: String,
        parentId: String?,
        currentUser: User
    ): Comment {
        val trimmed = body.trim()
        if (trimmed.isBlank()) error("评论不能为空")

        val inserted = supabase.from("resorts_post_comments")
            .insert(
                PostCommentInsert(
                    post_id = postId,
                    user_id = currentUser.id,
                    parent_comment_id = parentId,
                    body = trimmed
                )
            ) { select() }
            .decodeSingle<PostCommentRow>()

        return Comment(
            id = inserted.id,
            postId = inserted.post_id,
            parentId = inserted.parent_comment_id,
            author = currentUser,
            createdAt = timeAgo(inserted.created_at),
            body = inserted.body,
            likeCount = 0,
            isLikedByMe = false,
            canDelete = true
        )
    }

    override suspend fun deleteComment(commentId: String, currentUserId: String) {
        supabase.from("resorts_post_comments").delete {
            filter {
                eq("id", commentId)
                eq("user_id", currentUserId)
            }
        }
    }

    override suspend fun toggleCommentLike(commentId: String, currentUserId: String): Comment? {
        val exists = supabase.from("resorts_comment_likes")
            .select(Columns.Companion.raw("comment_id, user_id")) {
                filter {
                    eq("comment_id", commentId)
                    eq("user_id", currentUserId)
                }
                limit(1)
            }.decodeList<CommentLikeRow>()
            .isNotEmpty()

        if (!exists) {
            supabase.from("resorts_comment_likes")
                .insert(mapOf("comment_id" to commentId, "user_id" to currentUserId))
        } else {
            supabase.from("resorts_comment_likes")
                .delete {
                    filter {
                        eq("comment_id", commentId)
                        eq("user_id", currentUserId)
                    }
                }
        }

        // 返回 updated comment：简单粗暴再拉一次该帖评论（你也可以写一个 getCommentById 精简）
        val commentRow = supabase.from("resorts_post_comments")
            .select(Columns.Companion.raw("id, post_id, user_id, parent_comment_id, body, created_at")) {
                filter { eq("id", commentId) }
                limit(1)
            }.decodeList<PostCommentRow>()
            .firstOrNull() ?: return null

        val list = getCommentsForPost(commentRow.post_id)
        return list.firstOrNull { it.id == commentId }
    }
}