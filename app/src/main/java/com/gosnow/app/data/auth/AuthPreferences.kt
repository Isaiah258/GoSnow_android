package com.gosnow.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth")

class AuthPreferences(private val context: Context) {

    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val emailKey = stringPreferencesKey("email")

    val session: Flow<AuthSession?> = context.authDataStore.data.map { preferences ->
        val accessToken = preferences[accessTokenKey]
        val refreshToken = preferences[refreshTokenKey]
        val userId = preferences[userIdKey]
        val email = preferences[emailKey]

        if (accessToken.isNullOrBlank() || userId.isNullOrBlank()) {
            null
        } else {
            AuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                email = email.orEmpty()
            )
        }
    }

    suspend fun saveSession(authSession: AuthSession) {
        context.authDataStore.edit { preferences ->
            preferences[accessTokenKey] = authSession.accessToken
            if (authSession.refreshToken.isNullOrBlank()) {
                preferences.remove(refreshTokenKey)
            } else {
                preferences[refreshTokenKey] = authSession.refreshToken
            }
            preferences[userIdKey] = authSession.userId
            preferences[emailKey] = authSession.email
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(userIdKey)
            preferences.remove(emailKey)
        }
    }
}
