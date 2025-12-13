package com.gosnow.app.datasupabase

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object FeedbackRepository {

    private val client get() = SupabaseClientProvider.supabaseClient

    @Serializable
    private data class FeedbackInsertPayload(
        @SerialName("content") val content: String?,
        @SerialName("contact") val contact: String?
    )

    suspend fun submitFeedback(content: String, contact: String?) = withContext(Dispatchers.IO) {
        val payload = FeedbackInsertPayload(
            content = content.trim().ifBlank { null },
            contact = contact?.trim()?.ifBlank { null }
        )

        client.postgrest["FeedBackForUs"].insert(payload)
    }
}


