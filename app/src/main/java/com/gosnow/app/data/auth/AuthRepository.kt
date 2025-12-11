package com.gosnow.app.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.user.UserInfo

class AuthRepository(private val supabaseClient: SupabaseClient) {

    suspend fun sendOtpToPhone(phone: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(OTP) {
            this.phone = phone
        }
    }.map { }

    suspend fun verifyOtpAndLogin(phone: String, code: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(OTP) {
            this.phone = phone
            this.token = code
        }
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
