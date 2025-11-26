package com.gosnow.app.data.auth

import com.gosnow.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthApiService(
    private val client: OkHttpClient = OkHttpClient()
) {

    class AuthApiException(message: String) : Exception(message)

    suspend fun login(email: String, password: String): AuthSession = withContext(Dispatchers.IO) {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
            throw AuthApiException("Supabase 配置缺失，请在 local.properties 或环境变量中设置 SUPABASE_URL 与 SUPABASE_ANON_KEY")
        }

        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()

        val request = Request.Builder()
            .url("${BuildConfig.SUPABASE_URL}/auth/v1/token?grant_type=password")
            .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            val message = errorBody?.let { parseErrorMessage(it) }
                ?: "登录失败（${response.code}）"
            throw AuthApiException(message)
        }

        val responseBody = response.body?.string()
            ?: throw AuthApiException("登录失败：服务器返回空响应")

        val json = JSONObject(responseBody)
        val accessToken = json.optString("access_token")
        val refreshToken = json.optString("refresh_token")
        val user = json.optJSONObject("user")
        val userId = user?.optString("id") ?: ""
        val emailFromServer = user?.optString("email") ?: email

        if (accessToken.isBlank() || userId.isBlank()) {
            throw AuthApiException("登录响应不完整，请稍后重试")
        }

        AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = emailFromServer
        )
    }

    private fun parseErrorMessage(body: String): String? {
        return try {
            val json = JSONObject(body)
            json.optString("error_description")
                .ifBlank { json.optString("message") }
                .ifBlank { json.optString("error") }
        } catch (e: Exception) {
            null
        }
    }
}
