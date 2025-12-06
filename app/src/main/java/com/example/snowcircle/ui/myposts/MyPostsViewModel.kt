package com.example.snowcircle.ui.myposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snowcircle.data.PostRepository
import com.example.snowcircle.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyPostsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val posts: List<Post> = emptyList()
)

class MyPostsViewModel(
    private val postRepository: PostRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState(isLoading = true))
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { postRepository.getMyPosts(currentUserId) }
                .onSuccess { posts -> _uiState.update { it.copy(isLoading = false, posts = posts) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(posts = it.posts.filterNot { post -> post.id == postId }) }
            runCatching { postRepository.deletePost(postId, currentUserId) }
                .onFailure { refresh() }
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
