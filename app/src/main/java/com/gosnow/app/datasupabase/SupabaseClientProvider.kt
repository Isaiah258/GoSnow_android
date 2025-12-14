package com.gosnow.app.datasupabase

import android.content.Context
import com.gosnow.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SupabaseClientProvider {

    @Volatile
    private var _client: SupabaseClient? = null

    val supabaseClient: SupabaseClient
        get() = requireNotNull(_client) {
            "SupabaseClientProvider not initialized. Call SupabaseClientProvider.init(context) in Application.onCreate()."
        }

    @OptIn(SupabaseInternal::class)
    fun init(context: Context) {
        if (_client != null) return
        synchronized(this) {
            if (_client != null) return

            _client = createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth) {
                    autoLoadFromStorage = true
                    alwaysAutoRefresh = true
                }
                install(Postgrest)
                install(Storage)

                httpConfig {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                explicitNulls = false
                            }
                        )
                    }
                    defaultRequest {
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                    }
                }

                httpEngine = Android.create()
            }

        }
    }
}
