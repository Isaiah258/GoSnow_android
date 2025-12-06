package com.example.snowcircle.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snowcircle.data.NotificationsRepository
import com.example.snowcircle.data.PostRepository
import com.example.snowcircle.model.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val resortList = listOf("Niseko", "Hakuba", "Whistler", "Zermatt", "Aspen", "Stowe")

data class FeedUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val query: String = "",
    val selectedResort: String? = null,
    val posts: List<Post> = emptyList(),
    val resortSuggestions: List<String> = emptyList(),
    val hasUnreadNotifications: Boolean = false
)

open class FeedViewModel(
    private val postRepository: PostRepository,
    private val notificationsRepository: NotificationsRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            val notifications = notificationsRepository.getNotifications(currentUserId)
            _uiState.update { it.copy(hasUnreadNotifications = notifications.any { item -> !item.isRead }) }
        }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val posts = postRepository.getFeedPosts(_uiState.value.selectedResort)
                posts
            }.onSuccess { posts ->
                _uiState.update { state -> state.copy(isLoading = false, posts = filter(posts)) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Load failed") }
            }
        }
    }

    fun onQueryChange(text: String) {
        _uiState.update { state ->
            state.copy(
                query = text,
                selectedResort = if (state.selectedResort != null && text.isNotBlank()) state.selectedResort else state.selectedResort,
                resortSuggestions = if (text.isBlank()) emptyList() else resortList.filter { it.contains(text, true) }
            )
        }
        val currentPosts = _uiState.value.posts
        if (!_uiState.value.isLoading) {
            _uiState.update { it.copy(posts = filter(currentPosts)) }
        }
    }

    fun onResortSelected(resort: String?) {
        _uiState.update { it.copy(selectedResort = resort, query = "") }
        refresh()
    }

    fun onToggleLike(postId: String) {
        viewModelScope.launch {
            val updated = postRepository.toggleLike(postId, currentUserId)
            if (updated != null) {
                _uiState.update { state ->
                    state.copy(posts = state.posts.map { if (it.id == postId) updated else it })
                }
            }
        }
    }

    fun onNotificationsViewed() {
        _uiState.update { it.copy(hasUnreadNotifications = false) }
    }

    private fun filter(posts: List<Post>): List<Post> {
        val query = _uiState.value.query
        if (query.isBlank()) return posts
        return posts.filter { post ->
            post.content.contains(query, true) ||
                post.author.displayName.contains(query, true) ||
                (post.resortName?.contains(query, true) == true)
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    this.value = block(this.value)
}
