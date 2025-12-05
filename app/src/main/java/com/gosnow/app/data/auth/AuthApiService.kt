package com.gosnow.app.data.auth

import com.gosnow.app.BuildConfig
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Response

/**
 * 直接调用 Memfire / Supabase 的 auth/v1 接口：
 * - POST /otp    发送短信验证码
 * - POST /verify 校验验证码并返回 session
 */
class AuthApiService {

    class AuthApiException(message: String, val statusCode: Int? = null) : Exception(message)

    // 暴露给 Repository 用的 Session 类型
    data class AuthSession(
        val accessToken: String,
        val refreshToken: String,
        val userId: String?
    )

    // ------------------ Retrofit 初始化 ------------------

    private val api: SupabaseAuthApi

    init {
        // 确保没有重复的 '/'
        val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(SupabaseAuthApi::class.java)
    }

    // ------------------ 对外方法：发送验证码 ------------------

    suspend fun sendSmsCode(phone: String) {
        val apiKey = BuildConfig.SUPABASE_ANON_KEY
        val body = SendOtpRequest(
            phone = "+86$phone",
            type = "sms"
        )

        val resp = api.sendOtp(
            body = body,
            apiKey = apiKey,
            authorization = "Bearer $apiKey"
        )

        if (!resp.isSuccessful) {
            throw resp.toAuthException(defaultMsg = "验证码发送失败，请稍后重试")
        }
    }

    // ------------------ 对外方法：校验验证码并登录 ------------------

    suspend fun loginWithSms(phone: String, code: String): AuthSession {
        val apiKey = BuildConfig.SUPABASE_ANON_KEY
        val body = VerifyOtpRequest(
            type = "sms",
            phone = "+86$phone",
            token = code
        )

        val resp = api.verifyOtp(
            body = body,
            apiKey = apiKey,
            authorization = "Bearer $apiKey"
        )

        if (!resp.isSuccessful) {
            throw resp.toAuthException(defaultMsg = "登录失败，请稍后重试")
        }

        val auth = resp.body()
            ?: throw AuthApiException("登录失败：服务器返回为空", resp.code())

        return AuthSession(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId = auth.user?.id
        )
    }

    // ------------------ Retrofit 定义 ------------------

    private interface SupabaseAuthApi {

        @POST("otp")
        suspend fun sendOtp(
            @Body body: SendOtpRequest,
            @Header("apikey") apiKey: String,
            @Header("Authorization") authorization: String
        ): Response<Unit>

        @POST("verify")
        suspend fun verifyOtp(
            @Body body: VerifyOtpRequest,
            @Header("apikey") apiKey: String,
            @Header("Authorization") authorization: String
        ): Response<AuthResponse>
    }

    // 请求体
    data class SendOtpRequest(
        val phone: String,
        val type: String
    )

    data class VerifyOtpRequest(
        val type: String,   // 固定 "sms"
        val phone: String,
        val token: String
    )

    // Supabase verify 接口返回结构（只取我们需要的字段）
    data class AuthResponse(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("refresh_token") val refreshToken: String,
        @SerializedName("expires_in") val expiresIn: Long,
        @SerializedName("token_type") val tokenType: String,
        val user: SupabaseUser?
    )

    data class SupabaseUser(
        val id: String,
        val phone: String?,
        val email: String?
    )

    // 将 Response 转成我们自己的异常
    private fun <T> Response<T>.toAuthException(defaultMsg: String): AuthApiException {
        val msg = try {
            errorBody()?.string()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } ?: defaultMsg
        return AuthApiException(msg, code())
    }
}
