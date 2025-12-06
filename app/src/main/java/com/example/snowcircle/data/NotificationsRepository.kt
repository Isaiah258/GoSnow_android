package com.example.snowcircle.data

import com.example.snowcircle.model.NotificationItem

interface NotificationsRepository {
    suspend fun getNotifications(currentUserId: String): List<NotificationItem>
    suspend fun markAllRead(currentUserId: String)
    suspend fun markRead(notificationId: Long)
}
