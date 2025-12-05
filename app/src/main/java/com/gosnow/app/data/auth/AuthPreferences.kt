package com.gosnow.app.data.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 用 SharedPreferences 持久化 Supabase 的 access_token / refresh_token。
 * 暴露 session: StateFlow<AuthApiService.AuthSession?>
 */
class AuthPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<AuthApiService.AuthSession?> = _session.asStateFlow()

    private fun loadSession(): AuthApiService.AuthSession? {
        val access = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)

        return if (access != null && refresh != null) {
            AuthApiService.AuthSession(
                accessToken = access,
                refreshToken = refresh,
                userId = userId
            )
        } else {
            null
        }
    }

    suspend fun saveSession(session: AuthApiService.AuthSession) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_USER_ID, session.userId)
            .apply()
        _session.value = session
    }

    suspend fun clearSession() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }
}
