package com.example.snowcircle.data

import com.example.snowcircle.model.Comment
import com.example.snowcircle.model.User

interface CommentRepository {
    suspend fun getCommentsForPost(postId: String): List<Comment>
    suspend fun addComment(postId: String, body: String, parentId: String?, currentUser: User): Comment
    suspend fun deleteComment(commentId: String, currentUserId: String)
    suspend fun toggleCommentLike(commentId: String, currentUserId: String): Comment?
}
