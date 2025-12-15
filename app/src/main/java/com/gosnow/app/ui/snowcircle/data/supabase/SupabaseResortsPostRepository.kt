package com.gosnow.app.ui.snowcircle.data.supabase

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.gosnow.app.ui.snowcircle.data.PostRepository
import com.gosnow.app.ui.snowcircle.model.Post
import com.gosnow.app.ui.snowcircle.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class SupabaseResortsPostRepository(
    private val context: Context,
    private val supabase: SupabaseClient,
    private val postMediaBucket: String = "post-media",
    // 如果你的 bucket 是 public：用这个拼 public url（最省事）
    private val publicStorageBaseUrl: String = "" // 例如：https://xxxx.baseapi.memfiredb.com/storage/v1/object/public
) : PostRepository {

    override suspend fun getFeedPosts(resortFilter: String?): List<Post> {
        val myId = supabase.auth.currentUserOrNull()?.id
        val resortIds: List<Long>? = resortFilter?.takeIf { it.isNotBlank() }?.let { query ->
            val matched = supabase.from("Resorts_data")
                .select(Columns.Companion.raw("id, name_resort")) {
                    // 模糊搜索：name ilike %query%
                    // supabase-kt 用 ilike("name", "%xxx%")，这里为了稳就用 eq/like 你也可以换成 ilike
                    // 如果你已经在项目里用过 ilike，就把下面一行替换成 ilike("name", "%$query%")
                    filter { ilike("name_resort", "%$query%") }
                    limit(30)
                }.decodeList<ResortRow>()
            matched.map { it.id }
        }

        if (resortFilter != null && resortFilter.isNotBlank() && resortIds != null && resortIds.isEmpty()) {
            return emptyList()
        }

        val posts = supabase.from("resorts_post")
            .select(Columns.Companion.raw("id, created_at, author_id, resort_id, title, body, rating")) {
                if (resortIds != null) {
                    filter { isIn("resort_id", resortIds) }
                }
                order("created_at", Order.DESCENDING)
                limit(50)
            }.decodeList<ResortPostRow>()

        return enrichPosts(posts, myId)
    }

    override suspend fun getMyPosts(currentUserId: String): List<Post> {
        val posts = supabase.from("resorts_post")
            .select(Columns.Companion.raw("id, created_at, author_id, resort_id, title, body, rating")) {
                filter { eq("author_id", currentUserId) }
                order("created_at", Order.DESCENDING)
                limit(50)
            }.decodeList<ResortPostRow>()

        return enrichPosts(posts, currentUserId)
    }

    override suspend fun getPostById(postId: String): Post? {
        val myId = supabase.auth.currentUserOrNull()?.id
        val row = supabase.from("resorts_post")
            .select(Columns.Companion.raw("id, created_at, author_id, resort_id, title, body, rating")) {
                filter { eq("id", postId) }
                limit(1)
            }.decodeList<ResortPostRow>()
            .firstOrNull() ?: return null

        return enrichPosts(listOf(row), myId).firstOrNull()
    }

    override suspend fun toggleLike(postId: String, currentUserId: String): Post? {
        val existing = supabase.from("resorts_post_likes")
            .select(Columns.Companion.raw("post_id, author_id")) {
                filter {
                    eq("post_id", postId)
                    eq("author_id", currentUserId)
                }
                limit(1)
            }.decodeList<PostLikeRow>()

        if (existing.isEmpty()) {
            // like
            supabase.from("resorts_post_likes")
                .insert(mapOf("post_id" to postId, "author_id" to currentUserId))
        } else {
            // unlike
            supabase.from("resorts_post_likes")
                .delete {
                    filter {
                        eq("post_id", postId)
                        eq("author_id", currentUserId)
                    }
                }
        }
        return getPostById(postId)
    }

    override suspend fun deletePost(postId: String, currentUserId: String) {
        // 依赖 RLS + 这里再加一层 author_id 限制
        supabase.from("resorts_post").delete {
            filter {
                eq("id", postId)
                eq("author_id", currentUserId)
            }
        }
        // images / likes / comments 都是 cascade 或外键删除，不用你手动删
    }

    override suspend fun createPost(
        content: String,
        resortName: String?,
        images: List<String>,
        currentUser: User
    ): Post {
        val name = resortName?.trim().orEmpty()
        if (name.isBlank()) error("必须选择雪场")
        if (content.isBlank()) error("正文不能为空")

        // 1) resortName -> resort_id
        val resort = supabase.from("Resorts_data")
            .select(Columns.Companion.raw("id, name_resort")) {
                filter { ilike("name_resort", "%$name%") }
                limit(1)
            }.decodeList<ResortRow>()
            .firstOrNull() ?: error("没找到该雪场：$name")

        // 2) insert post
        val inserted = supabase.from("resorts_post")
            .insert(
                ResortPostInsert(
                    resort_id = resort.id,
                    body = content
                )
            ) { select() }
            .decodeSingle<ResortPostRow>()

        // 3) upload images + insert resorts_post_images
        val uris = images.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }.take(4)
        if (uris.isNotEmpty()) {
            val rows = mutableListOf<PostImageInsert>()
            uris.forEachIndexed { index, uri ->
                val ext = guessExt(uri)
                val path = "${currentUser.id}/${inserted.id}/${System.currentTimeMillis()}_${index}.${ext}"
                val bytes = readBytes(uri)
                // upload
                supabase.storage.from(postMediaBucket).upload(path, bytes, upsert = true)
                val url = buildPublicUrl(path)
                rows += PostImageInsert(
                    post_id = inserted.id,
                    user_id = currentUser.id,
                    url = url
                )
            }
            supabase.from("resorts_post_images").insert(rows)
        }

        return getPostById(inserted.id) ?: error("发布成功但读取失败")
    }

    // ---------------- helpers ----------------

    private suspend fun enrichPosts(rows: List<ResortPostRow>, myId: String?): List<Post> {
        if (rows.isEmpty()) return emptyList()

        val postIds = rows.map { it.id }
        val authorIds = rows.mapNotNull { it.author_id }.distinct()
        val resortIds = rows.mapNotNull { it.resort_id }.distinct()

        val usersMap = if (authorIds.isEmpty()) emptyMap() else {
            supabase.from("Users")
                .select(Columns.Companion.raw("id, user_name, avatar_url")) {
                    filter { isIn("id", authorIds) }
                }.decodeList<UserRow>()
                .associateBy { it.id }
        }

        val resortsMap = if (resortIds.isEmpty()) emptyMap() else {
            supabase.from("Resorts_data")
                .select(Columns.Companion.raw("id, name_resort")) {
                    filter { isIn("id", resortIds) }
                }.decodeList<ResortRow>()
                .associateBy { it.id }
        }

        val imagesMap = supabase.from("resorts_post_images")
            .select(Columns.Companion.raw("id, post_id, url")) {
                filter { isIn("post_id", postIds) }   // 批量 in 查询 :contentReference[oaicite:1]{index=1}
            }.decodeList<PostImageRow>()
            .groupBy { it.post_id }

        val likes = supabase.from("resorts_post_likes")
            .select(Columns.Companion.raw("post_id, author_id")) {
                filter { isIn("post_id", postIds) }
            }.decodeList<PostLikeRow>()

        val likeCountMap = likes.groupingBy { it.post_id }.eachCount()
        val likedByMeSet = myId?.let { me ->
            likes.filter { it.author_id == me }.map { it.post_id }.toSet()
        } ?: emptySet()

        val comments = supabase.from("resorts_post_comments")
            .select(Columns.Companion.raw("id, post_id")) {
                filter { isIn("post_id", postIds) }
            }.decodeList<Map<String, String>>() // 只要 post_id 就行，简单点
        val commentCountMap = comments.groupingBy { it["post_id"].orEmpty() }.eachCount()

        return rows.map { r ->
            val authorRow = r.author_id?.let { usersMap[it] }
            val author = User(
                id = r.author_id ?: "",
                displayName = authorRow?.user_name ?: "Unknown",
                avatarUrl = authorRow?.avatar_url
            )
            val resortName = r.resort_id?.let { resortsMap[it]?.name }
            val imageUrls = imagesMap[r.id].orEmpty().mapNotNull { it.url }

            Post(
                id = r.id,
                author = author,
                resortName = resortName,
                createdAt = timeAgo(r.created_at),
                content = r.body ?: "",
                imageUrls = imageUrls,
                likeCount = likeCountMap[r.id] ?: 0,
                commentCount = commentCountMap[r.id] ?: 0,
                isLikedByMe = likedByMeSet.contains(r.id),
                canDelete = (myId != null && r.author_id == myId)
            )
        }
    }

    private fun buildPublicUrl(path: String): String {
        // 你如果 bucket 是 public：publicStorageBaseUrl = "${SUPABASE_URL}/storage/v1/object/public"
        // 返回：{publicStorageBaseUrl}/{bucket}/{path}
        return if (publicStorageBaseUrl.isNotBlank()) {
            "${publicStorageBaseUrl.trimEnd('/')}/${postMediaBucket}/${path}"
        } else {
            // 兜底：只存 path（不推荐）。你也可以这里 error 强制传 baseUrl
            path
        }
    }

    private suspend fun readBytes(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("读取图片失败")
    }

    private fun guessExt(uri: Uri): String {
        val resolver = context.contentResolver
        val type = resolver.getType(uri) ?: return "jpg"
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "jpg"
    }
}