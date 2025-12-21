package com.gosnow.app.data.update

import com.gosnow.app.datasupabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateRepository {
    private val client get() = SupabaseClientProvider.supabaseClient

    suspend fun checkUpdate(): AppUpdateNotice? = withContext(Dispatchers.IO) {
        try {
            val result = client.from("app_update_notice")
                .select {
                    filter {
                        eq("platform", "android")
                        eq("is_active", true)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeList<AppUpdateNotice>()

            return@withContext result.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}