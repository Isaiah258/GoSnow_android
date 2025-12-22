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
// 用于本地模拟筛选，实际应该靠数据库查询
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
    val visibleCount: Int = PAGE_SIZE,
    // 新增：用于显示简单的提示信息 (如“举报成功”)
    val userMessage: String? = null
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
            runCatching { notificationsRepository.getNotifications(currentUserId) }
                .onSuccess { items ->
                    _uiState.update { it.copy(hasUnreadNotifications = items.any { item -> !item.isRead }) }
                }
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
                selectedResort = state.selectedResort,
                resortSuggestions = if (text.isBlank()) emptyList() else resortList.filter { it.contains(text, ignoreCase = true) }
            )
        }
        if (!_uiState.value.isLoading) {
            val filtered = filter(fullFeed)
            _uiState.update { it.copy(posts = filtered, visibleCount = min(PAGE_SIZE, filtered.size)) }
        }
    }

    fun onResortSelected(resort: String?) {
        _uiState.update { it.copy(selectedResort = resort, query = "") }
        refresh()
    }

    fun onLoadMore() {
        _uiState.update { state ->
            val newCount = (state.visibleCount + PAGE_SIZE).coerceAtMost(state.posts.size)
            state.copy(visibleCount = newCount)
        }
    }

    fun onToggleLike(postId: String) {
        viewModelScope.launch {
            val updated = postRepository.toggleLike(postId, currentUserId)
            if (updated != null) {
                // 更新本地列表状态
                updatePostInList(updated)
            }
        }
    }

    // ✅ 新增：删除帖子逻辑
    fun onDeletePost(postId: String) {
        viewModelScope.launch {
            runCatching {
                postRepository.deletePost(postId, currentUserId)
            }.onSuccess {
                // 删除成功后，从本地列表中移除
                _uiState.update { state ->
                    val newPosts = state.posts.filter { it.id != postId }
                    fullFeed = fullFeed.filter { it.id != postId }
                    state.copy(posts = newPosts, userMessage = "帖子已删除")
                }
            }.onFailure {
                _uiState.update { it.copy(userMessage = "删除失败，请稍后重试") }
            }
        }
    }

    // ✅ 新增：举报帖子逻辑 (仅模拟)
    fun onReportPost(postId: String) {
        // 实际开发中这里应调用 postRepository.reportPost(postId)
        _uiState.update { it.copy(userMessage = "已举报，我们会尽快处理") }
    }

    fun onNotificationsViewed() {
        _uiState.update { it.copy(hasUnreadNotifications = false) }
    }

    // 清除一次性消息
    fun messageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun updatePostInList(updated: Post) {
        _uiState.update { state ->
            state.copy(posts = state.posts.map { if (it.id == updated.id) updated else it })
        }
        // 同时更新缓存源
        fullFeed = fullFeed.map { if (it.id == updated.id) updated else it }
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