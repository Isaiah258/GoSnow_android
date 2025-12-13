package com.gosnow.app.datasupabase

import android.content.Context
import com.gosnow.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
//import io.github.jan.supabase.gotrue.storage.SharedPreferencesSessionManager
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android

/**
 * 负责创建并持有全局 SupabaseClient 实例（支持 Session 持久化）。
 *
 * ✅ 必须在 Application.onCreate() 调用 init(context)
 */
object SupabaseClientProvider {

    @Volatile
    private var _client: SupabaseClient? = null

    val supabaseClient: SupabaseClient
        get() = requireNotNull(_client) {
            "SupabaseClientProvider not initialized. Call SupabaseClientProvider.init(context) in Application.onCreate()."
        }

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

                // 2.4.0 Ktor 配置
                httpEngine = Android.create()
            }
        }
    }
}
