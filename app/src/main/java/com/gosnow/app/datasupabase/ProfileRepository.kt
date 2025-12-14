package com.gosnow.app.datasupabase

import com.gosnow.app.BuildConfig
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 负责跟 Supabase "Users" 表 & 头像存储桶打交道。
 *
 * 注意：
 * - Auth & Storage 继续用 supabase-kt 2.4.0
 * - 对 Users 表的 CRUD 用 Ktor 直接打 REST（PostgREST），避免你之前那种序列化/插件差异问题。
 */
object ProfileRepository {

    // 复用全局 SupabaseClient
    private val supabaseClient get() = SupabaseClientProvider.supabaseClient

    // 你的头像桶名
    private const val AVATAR_BUCKET = "user"

    // PostgREST 路径：<SUPABASE_URL>/rest/v1/Users
    private const val USERS_TABLE_PATH = "/rest/v1/Users"

    private val baseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    // Ktor HttpClient：只初始化一次
    private val httpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }

            // ✅ 全局兜底：默认所有请求都带 JSON 头（避免 Content-Type: null）
            defaultRequest {
                header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                // 注意：GET 请求不一定需要 Content-Type，但加了也无害；
                // 真正关键是 POST/PATCH 的 Content-Type 必须是 JSON。
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
        }
    }

    // ===== DTO =====

    @Serializable
    private data class UsersRow(
        val id: String,
        @SerialName("user_name") val userName: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null
    )

    @Serializable
    private data class UsersInsert(
        val id: String,
        @SerialName("user_name") val userName: String,
        @SerialName("avatar_url") val avatarUrl: String? = null
    )

    @Serializable
    private data class UsersPatch(
        @SerialName("user_name") val userName: String,
        @SerialName("avatar_url") val avatarUrl: String?
    )

    // ------------ 1) 登录后获取 / 创建当前用户资料 ------------

    suspend fun getOrCreateCurrentUserProfile(): CurrentUserProfile =
        withContext(Dispatchers.IO) {

            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("未登录，无法获取资料")

            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw IllegalStateException("当前没有有效登录会话")

            val userId = user.id
            val accessToken = session.accessToken

            // ① 先尝试从 Users 表读取
            val existing: List<UsersRow> =
                httpClient.get("$baseUrl$USERS_TABLE_PATH") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")

                    parameter("id", "eq.$userId")
                    parameter("select", "id,user_name,avatar_url")
                    parameter("limit", 1)
                }.body()

            if (existing.isNotEmpty()) {
                val row = existing.first()
                return@withContext CurrentUserProfile(
                    id = row.id,
                    userName = row.userName ?: "雪友",
                    avatarUrl = row.avatarUrl
                )
            }

            // ② 不存在：插入默认昵称
            val defaultName = when {
                !user.phone.isNullOrBlank() -> "雪友${user.phone!!.takeLast(4)}"
                !user.email.isNullOrBlank() -> user.email!!.substringBefore("@")
                else -> "雪友"
            }

            val payload = UsersInsert(
                id = userId,
                userName = defaultName,
                avatarUrl = null
            )

            // PostgREST 插入：通常 body 要数组形式
            val inserted: List<UsersRow> =
                httpClient.post("$baseUrl$USERS_TABLE_PATH") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header("Prefer", "return=representation")

                    // 可选：限制返回字段
                    parameter("select", "id,user_name,avatar_url")

                    // ✅ 关键：这里是 listOf(payload)，并且 Content-Type 已由 defaultRequest 设置为 JSON
                    setBody(listOf(payload))
                }.body()

            val created = inserted.firstOrNull()
            return@withContext CurrentUserProfile(
                id = created?.id ?: userId,
                userName = created?.userName ?: defaultName,
                avatarUrl = created?.avatarUrl
            )
        }

    // ------------ 2) 更新昵称 + 头像 ------------

    /**
     * @param nickname         新昵称
     * @param avatarBytes      新头像（压缩后的 jpeg 字节）；null 表示没改头像
     * @param currentAvatarUrl 当前头像 URL（没改头像时沿用）
     * @return 最终生效的头像 URL（可能为 null）
     */
    suspend fun updateProfile(
        nickname: String,
        avatarBytes: ByteArray?,
        currentAvatarUrl: String?
    ): String? = withContext(Dispatchers.IO) {

        val user = supabaseClient.auth.currentUserOrNull()
            ?: throw IllegalStateException("未登录，无法更新资料")

        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("当前没有有效登录会话")

        val userId = user.id
        val accessToken = session.accessToken

        var finalAvatarUrl = currentAvatarUrl

        // 1️⃣ 上传新头像（如果有）
        if (avatarBytes != null) {
            val bucket = supabaseClient.storage.from(AVATAR_BUCKET)
            val path = "user-$userId/avatar-${System.currentTimeMillis()}.jpg"

            bucket.upload(path, avatarBytes)
            finalAvatarUrl = bucket.publicUrl(path)
        }

        // 2️⃣ PATCH Users 表
        val patch = UsersPatch(
            userName = nickname,
            avatarUrl = finalAvatarUrl
        )

        httpClient.patch("$baseUrl$USERS_TABLE_PATH") {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header("Prefer", "return=representation")

            parameter("id", "eq.$userId")

            // PATCH 的 body 必须是可序列化对象（这里就是 patch）
            setBody(patch)
        }.body<List<UsersRow>>() // 返回值你可以不使用

        return@withContext finalAvatarUrl
    }
}
