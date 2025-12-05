package com.gosnow.app.data.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel 用的仓库层：
 * - 暴露 session Flow
 * - 发送验证码
 * - 校验验证码并保存 Session
 * - 登出
 */
class AuthRepository(
    private val apiService: AuthApiService,
    private val preferences: AuthPreferences
) {

    // 注意：LoginViewModel 只是判断是否 null，不关心结构
    val session: StateFlow<AuthApiService.AuthSession?> = preferences.session

    suspend fun sendSmsCode(phone: String) {
        apiService.sendSmsCode(phone)
    }

    suspend fun loginWithSms(phone: String, code: String) {
        val session = apiService.loginWithSms(phone, code)
        preferences.saveSession(session)
    }

    suspend fun logout() {
        preferences.clearSession()
    }
}
