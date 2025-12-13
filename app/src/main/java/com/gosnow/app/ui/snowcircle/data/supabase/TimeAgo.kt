package com.gosnow.app.ui.snowcircle.data.supabase

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

fun timeAgo(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val instant = runCatching { OffsetDateTime.parse(iso).toInstant() }.getOrNull() ?: return ""
    val now = Instant.now()
    val d = Duration.between(instant, now)
    val minutes = d.toMinutes()
    val hours = d.toHours()
    val days = d.toDays()
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> iso.take(10)
    }
}


