package com.gosnow.app

import android.app.Application
import com.gosnow.app.datasupabase.SupabaseClientProvider

class GoSnowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(this)
        // 提前创建 SupabaseClient，确保全局单例就绪
        SupabaseClientProvider.supabaseClient

    }
}
