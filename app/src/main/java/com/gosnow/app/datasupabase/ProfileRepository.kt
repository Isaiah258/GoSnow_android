package com.gosnow.app.datasupabase

import com.gosnow.app.BuildConfig
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
 * - 对 Users 表的 CRUD 用 Ktor 直接打 REST，不再用 eq/decodeList，
 *   彻底绕开你现在遇到的版本差异问题。
 */
object ProfileRepository {

    // 复用全局 SupabaseClient
    private val supabaseClient get() = SupabaseClientProvider.supabaseClient

    // 你的头像桶名（看默认 URL 是 user/xxx.jpg，所以这里用 "user"）
    private const val AVATAR_BUCKET = "user"

    // REST 路径：<SUPABASE_URL>/rest/v1/Users
    private const val USERS_TABLE_PATH = "/rest/v1/Users"

    // Ktor HttpClient：只初始化一次
    private val httpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true  // 表里多字段也没关系
                    }
                )
            }
        }
    }

    private val baseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    // ------------ 1. 登录后获取 / 创建当前用户的 Users 记录 ------------

    suspend fun getOrCreateCurrentUserProfile(): CurrentUserProfile =
        withContext(Dispatchers.IO) {

            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("未登录，无法获取资料")

            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw IllegalStateException("当前没有有效登录会话")

            val userId = user.id
            val accessToken = session.accessToken

            // ① 先尝试从 Users 表读取一条记录
            val existing: List<CurrentUserProfile> =
                httpClient.get("$baseUrl$USERS_TABLE_PATH") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header("Authorization", "Bearer $accessToken")
                    // Postgrest 过滤：id=eq.<userId>
                    parameter("id", "eq.$userId")
                    parameter("select", "id,user_name,avatar_url")
                    parameter("limit", 1)
                }.body()

            if (existing.isNotEmpty()) {
                return@withContext existing.first()
            }

            // ② 没有记录：插入一条带默认昵称的
            val defaultName = when {
                !user.phone.isNullOrBlank() -> "雪友${user.phone!!.takeLast(4)}"
                !user.email.isNullOrBlank() -> user.email!!.substringBefore("@")
                else -> "雪友"
            }

            // avatarUrl 传 null，让表里自己的 default 生效
            val newProfile = CurrentUserProfile(
                id = userId,
                userName = defaultName,
                avatarUrl = null
            )

            // 插入时，Postgrest 需要数组形式：[{...}]
            val inserted: List<CurrentUserProfile> =
                httpClient.post("$baseUrl$USERS_TABLE_PATH") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header("Authorization", "Bearer $accessToken")
                    header("Prefer", "return=representation")
                    setBody(listOf(newProfile))
                }.body()

            return@withContext inserted.firstOrNull() ?: newProfile
        }

    // ------------ 2. 更新昵称 + 头像 ------------

    @Serializable
    private data class UserProfilePatch(
        @SerialName("user_name")
        val userName: String,
        @SerialName("avatar_url")
        val avatarUrl: String?
    )

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

            // 上传到 Storage
            bucket.upload(path, avatarBytes)

            // 桶是 public 的话，这里就是公网 URL
            finalAvatarUrl = bucket.publicUrl(path)
        }

        // 2️⃣ PATCH Users 表（只修改 user_name / avatar_url 两个字段）
        val patch = UserProfilePatch(
            userName = nickname,
            avatarUrl = finalAvatarUrl
        )

        httpClient.patch("$baseUrl$USERS_TABLE_PATH") {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer $accessToken")
            header("Prefer", "return=representation")
            parameter("id", "eq.$userId")
            setBody(patch)
        }.body<List<CurrentUserProfile>>()  // 返回值你现在用不到，直接丢掉

        return@withContext finalAvatarUrl
    }
}
