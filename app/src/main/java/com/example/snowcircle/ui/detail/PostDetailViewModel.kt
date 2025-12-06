package com.example.snowcircle.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snowcircle.data.CommentRepository
import com.example.snowcircle.data.PostRepository
import com.example.snowcircle.data.currentUser
import com.example.snowcircle.model.Comment
import com.example.snowcircle.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommentThreadUiState(
    val roots: List<Comment> = emptyList(),
    val children: Map<String, List<Comment>> = emptyMap()
)

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val post: Post? = null,
    val comments: CommentThreadUiState = CommentThreadUiState(),
    val replyTarget: Comment? = null
)

class PostDetailViewModel(
    private val postId: String,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState(isLoading = true))
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val post = postRepository.getPostById(postId)
            val comments = commentRepository.getCommentsForPost(postId)
            if (post == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Post missing") }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, post = post, comments = buildThread(comments))
                }
            }
        }
    }

    fun onTogglePostLike() {
        viewModelScope.launch {
            val updated = postRepository.toggleLike(postId, currentUserId)
            updated?.let { _uiState.update { state -> state.copy(post = it, comments = state.comments) } }
        }
    }

    fun onToggleCommentLike(commentId: String) {
        viewModelScope.launch {
            val updated = commentRepository.toggleCommentLike(commentId, currentUserId)
            updated?.let { comment ->
                val current = uiState.value.comments
                val newRoots = current.roots.map { if (it.id == comment.id) comment else it }
                val newChildren = current.children.mapValues { (parent, list) ->
                    if (parent == comment.parentId) list.map { if (it.id == comment.id) comment else it } else list
                }
                _uiState.update { it.copy(comments = current.copy(roots = newRoots, children = newChildren)) }
            }
        }
    }

    fun onSendComment(text: String, parentId: String?) {
        viewModelScope.launch {
            val newComment = commentRepository.addComment(postId, text, parentId, currentUser())
            val current = uiState.value.comments
            if (parentId == null) {
                _uiState.update { it.copy(comments = current.copy(roots = listOf(newComment) + current.roots)) }
            } else {
                val children = current.children.toMutableMap()
                val replies = children[parentId].orEmpty()
                children[parentId] = listOf(newComment) + replies
                _uiState.update { it.copy(comments = current.copy(children = children)) }
            }
            _uiState.update { it.copy(replyTarget = null) }
        }
    }

    fun onDeleteComment(commentId: String) {
        viewModelScope.launch {
            commentRepository.deleteComment(commentId, currentUserId)
            refresh()
        }
    }

    fun onReplyTargetSelected(comment: Comment?) {
        _uiState.update { it.copy(replyTarget = comment) }
    }

    private fun buildThread(comments: List<Comment>): CommentThreadUiState {
        val roots = comments.filter { it.parentId == null }
        val children = comments.filter { it.parentId != null }.groupBy { it.parentId!! }
        return CommentThreadUiState(roots, children)
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
