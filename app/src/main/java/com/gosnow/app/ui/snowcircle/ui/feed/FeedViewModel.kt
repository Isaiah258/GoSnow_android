package com.gosnow.app.ui.snowcircle.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosnow.app.ui.snowcircle.data.NotificationsRepository
import com.gosnow.app.ui.snowcircle.data.PostRepository
import com.gosnow.app.ui.snowcircle.model.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min
private const val PAGE_SIZE = 20
private var fullFeed: List<Post> = emptyList()

private val resortList = listOf("Niseko", "Hakuba", "Whistler", "Zermatt", "Aspen", "Stowe")

data class FeedUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val query: String = "",
    val selectedResort: String? = null,
    val posts: List<Post> = emptyList(),
    val resortSuggestions: List<String> = emptyList(),
    val hasUnreadNotifications: Boolean = false,
    val visibleCount: Int = PAGE_SIZE
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



    open fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                postRepository.getFeedPosts(_uiState.value.selectedResort)
            }.onSuccess { posts ->
                fullFeed = posts
                val filtered = filter(fullFeed)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        posts = filtered,
                        visibleCount = min(PAGE_SIZE, filtered.size)
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Load failed"
                    )
                }
            }
        }
    }


    fun onQueryChange(text: String) {
        _uiState.update { state ->
            state.copy(
                query = text,
                // selectedResort 不跟着乱动，保持当前雪场过滤
                selectedResort = state.selectedResort,
                resortSuggestions = if (text.isBlank()) {
                    emptyList()
                } else {
                    resortList.filter { it.contains(text, ignoreCase = true) }
                }
            )
        }

        if (!_uiState.value.isLoading) {
            val filtered = filter(fullFeed)
            _uiState.update {
                it.copy(
                    posts = filtered,
                    visibleCount = min(PAGE_SIZE, filtered.size)
                )
            }
        }
    }


    fun onResortSelected(resort: String?) {
        _uiState.update { it.copy(selectedResort = resort, query = "") }
        refresh()
    }
    fun onLoadMore() {
        _uiState.update { state ->
            val newCount = (state.visibleCount + PAGE_SIZE)
                .coerceAtMost(state.posts.size)
            state.copy(visibleCount = newCount)
        }
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
