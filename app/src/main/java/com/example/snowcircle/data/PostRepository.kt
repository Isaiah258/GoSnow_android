package com.example.snowcircle.data

import com.example.snowcircle.model.Post
import com.example.snowcircle.model.User

interface PostRepository {
    suspend fun getFeedPosts(resortFilter: String? = null): List<Post>
    suspend fun getMyPosts(currentUserId: String): List<Post>
    suspend fun getPostById(postId: String): Post?
    suspend fun toggleLike(postId: String, currentUserId: String): Post?
    suspend fun deletePost(postId: String, currentUserId: String)
    suspend fun createPost(content: String, resortName: String?, images: List<String>, currentUser: User): Post
}
