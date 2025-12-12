package com.gosnow.app.data.auth

import com.gosnow.app.datasupabase.CurrentUserStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.providers.builtin.OTP

class AuthRepository(private val supabaseClient: SupabaseClient) {

    // 发送短信验证码
    suspend fun sendOtpToPhone(phone: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(OTP) {
            this.phone = phone           // 这里只负责“请求发送验证码”
        }
    }.map { }

    // 校验验证码并登录
    suspend fun verifyOtpAndLogin(phone: String, code: String): Result<Unit> = runCatching {
        supabaseClient.auth.verifyPhoneOtp(
            phone = phone,
            token = code,
            type = OtpType.Phone.SMS         // 关键：这里要传 type，见下面说明
        )

        CurrentUserStore.refreshFromServer()
    }.map { }

    suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
    }

    suspend fun currentUser(): Result<UserInfo?> = runCatching {
        supabaseClient.auth.currentUserOrNull()
    }

    suspend fun hasActiveSession(): Result<Boolean> = runCatching {
        supabaseClient.auth.currentSessionOrNull() != null
    }
}
