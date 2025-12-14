

package com.gosnow.app.data.auth

import com.gosnow.app.datasupabase.CurrentUserStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.user.UserInfo

class AuthRepository(private val supabaseClient: SupabaseClient) {

    suspend fun sendOtpToPhone(phone: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(OTP) {
            this.phone = phone
            createUser = true
        }
    }.map { }
        .mapErrorToUserMessage { mapSendOtpError(it) }

    suspend fun verifyOtpAndLogin(phone: String, code: String): Result<Unit> = runCatching {
        supabaseClient.auth.verifyPhoneOtp(
            phone = phone,
            token = code,
            type = OtpType.Phone.SMS
        )
        // 登录成功后再同步资料
        CurrentUserStore.refreshFromServer()
    }.map { }
        .mapErrorToUserMessage { mapVerifyOtpError(it) }

    suspend fun signOut(): Result<Unit> = runCatching { supabaseClient.auth.signOut() }

    suspend fun currentUser(): Result<UserInfo?> = runCatching { supabaseClient.auth.currentUserOrNull() }

    suspend fun hasActiveSession(): Result<Boolean> = runCatching { supabaseClient.auth.currentSessionOrNull() != null }

    // ------- 错误映射（核心） -------

    private fun mapSendOtpError(t: Throwable): String {
        val msg = t.message.orEmpty()
        val lower = msg.lowercase()

        return when {
            // 频率限制 / 太频繁（常见文案：rate limit / too many requests）
            "rate" in lower || "too many" in lower || "429" in lower ->
                "发送太频繁了，请稍后再试"

            // 手机号格式/不可用
            "phone" in lower && ("invalid" in lower || "format" in lower) ->
                "手机号格式不正确，请检查后重试"

            else ->
                "验证码发送失败，请稍后重试"
        }
    }

    private fun mapVerifyOtpError(t: Throwable): String {
        val msg = t.message.orEmpty()
        val lower = msg.lowercase()

        return when {
            // 你截图里的核心：Token has expired or is invalid
            ("token" in lower && "expired" in lower) || ("otp" in lower && "expired" in lower) ->
                "验证码已过期，请重新获取"

            ("token" in lower && "invalid" in lower) || ("otp" in lower && "invalid" in lower) ->
                "验证码不正确，请检查后重试"

            // 有些会返回“已使用/重复使用”
            "used" in lower || "already" in lower ->
                "验证码已失效，请重新获取"

            // 频率/风控
            "too many" in lower || "rate" in lower || "429" in lower ->
                "操作太频繁了，请稍后再试"

            else ->
                "登录失败，请稍后重试"
        }
    }
}

/** 把 Result.failure(Throwable) 统一替换成用户可读的 Throwable(message) */
private inline fun <T> Result<T>.mapErrorToUserMessage(
    mapper: (Throwable) -> String
): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(IllegalStateException(mapper(it))) }
    )
}







/*
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
            createUser = true
        }
    }.map { }

    // 校验验证码并登录
    suspend fun verifyOtpAndLogin(phone: String, code: String): Result<Unit> = runCatching {
        // 1) 先只验证 OTP
        supabaseClient.auth.verifyPhoneOtp(
            phone = phone,
            token = code,
            type = OtpType.Phone.SMS
        )
    }.mapCatching {
        // 2) 再做用户资料同步（这里最可能触发 SingletonList）
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
*/