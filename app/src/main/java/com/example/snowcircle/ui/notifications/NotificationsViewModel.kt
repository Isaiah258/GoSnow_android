package com.example.snowcircle.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snowcircle.data.NotificationsRepository
import com.example.snowcircle.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notifications: List<NotificationItem> = emptyList()
)

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { notificationsRepository.getNotifications(currentUserId) }
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, notifications = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            notificationsRepository.markAllRead(currentUserId)
            refresh()
        }
    }

    suspend fun onNotificationTapped(notificationId: Long): String? {
        notificationsRepository.markRead(notificationId)
        val notification = _uiState.value.notifications.find { it.id == notificationId }
        refresh()
        return notification?.postId
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
