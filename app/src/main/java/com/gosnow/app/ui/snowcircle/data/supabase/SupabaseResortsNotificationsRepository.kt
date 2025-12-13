package com.gosnow.app.ui.snowcircle.data.supabase

import com.gosnow.app.ui.snowcircle.data.NotificationsRepository
import com.gosnow.app.ui.snowcircle.model.NotificationItem
import com.gosnow.app.ui.snowcircle.model.NotificationType
import com.gosnow.app.ui.snowcircle.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.util.Objects.isNull

class SupabaseResortsNotificationsRepository(
    private val supabase: SupabaseClient
) : NotificationsRepository {

    override suspend fun getNotifications(currentUserId: String): List<NotificationItem> {
        val rows = supabase.from("resorts_notifications")
            .select(Columns.raw("id, recipient_user_id, actor_user_id, type, post_id, comment_id, created_at, read_at")) {
                filter { eq("recipient_user_id", currentUserId) }
                order("created_at", Order.DESCENDING)
                limit(100)
            }.decodeList<ResortNotificationRow>()

        if (rows.isEmpty()) return emptyList()

        val actorIds = rows.map { it.actor_user_id }.distinct()
        val users = supabase.from("Users")
            .select(Columns.raw("id, user_name, avatar_url")) {
                filter { isIn("id", actorIds) }
            }.decodeList<UserRow>()
            .associateBy { it.id }

        return rows.map { r ->
            val u = users[r.actor_user_id]
            val actor = User(
                id = r.actor_user_id,
                displayName = u?.user_name ?: "Unknown",
                avatarUrl = u?.avatar_url
            )
            NotificationItem(
                id = r.id,
                type = mapType(r.type),
                createdAt = timeAgo(r.created_at),
                postId = r.post_id,
                commentId = r.comment_id,
                actor = actor,
                isRead = (r.read_at != null)
            )
        }
    }

    override suspend fun markAllRead(currentUserId: String) {
        val now = Instant.now().toString()
        supabase.from("resorts_notifications")
            .update(MarkReadPatch(read_at = now)) {
                filter {
                    eq("recipient_user_id", currentUserId)
                    isNull("read_at")
                }
            }
    }

    override suspend fun markRead(notificationId: Long) {
        val now = Instant.now().toString()
        supabase.from("resorts_notifications")
            .update(MarkReadPatch(read_at = now)) {
                filter { eq("id", notificationId) }
            }
    }

    private fun mapType(db: String): NotificationType = when (db) {
        "like_post" -> NotificationType.LIKE_POST
        "like_comment" -> NotificationType.LIKE_COMMENT
        "comment_post" -> NotificationType.COMMENT_POST
        "reply_comment" -> NotificationType.REPLY_COMMENT
        else -> NotificationType.COMMENT_POST
    }
}


