package com.gosnow.app.data.auth

import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val apiService: AuthApiService,
    private val preferences: AuthPreferences
) {

    val session: Flow<AuthSession?> = preferences.session

    suspend fun login(email: String, password: String): AuthSession {
        val session = apiService.login(email = email, password = password)
        preferences.saveSession(session)
        return session
    }

    suspend fun logout() {
        preferences.clearSession()
    }
}
