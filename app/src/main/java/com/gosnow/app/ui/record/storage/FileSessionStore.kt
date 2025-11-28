package com.gosnow.app.ui.record.storage

import android.content.Context
import com.gosnow.app.recording.model.SkiSession
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_SESSIONS = 500

interface SessionStore {
    suspend fun saveSession(session: SkiSession)
    suspend fun loadSessions(): List<SkiSession>
}

/**
 * 和 iOS 的 JSONLocalStore 类似：
 * - /files/sessions/ 目录下，每个 Session 一个 .json 文件
 * - 超过 MAX_SESSIONS 会删除最旧的
 */
class FileSessionStore(
    context: Context
) : SessionStore {

    private val dir: File = File(context.filesDir, "sessions").apply {
        if (!exists()) mkdirs()
    }

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    override suspend fun saveSession(session: SkiSession) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(session)
            val file = File(dir, "${session.id}.json")
            file.writeText(json, Charsets.UTF_8)

            pruneToLimit(MAX_SESSIONS)
        }
    }

    override suspend fun loadSessions(): List<SkiSession> {
        return withContext(Dispatchers.IO) {
            val files = dir.listFiles { f -> f.extension == "json" } ?: emptyArray()

            files.mapNotNull { f ->
                runCatching {
                    val text = f.readText(Charsets.UTF_8)
                    gson.fromJson(text, SkiSession::class.java)
                }.getOrNull()
            }.sortedByDescending { it.startAtMillis }
        }
    }

    private fun pruneToLimit(max: Int) {
        if (max <= 0) return
        val files = dir.listFiles { f -> f.extension == "json" } ?: return
        if (files.size <= max) return

        // 先把文件解析成 (session, file)，按开始时间排
        val sessionsWithFile = files.mapNotNull { f ->
            runCatching {
                val text = f.readText(Charsets.UTF_8)
                val s = gson.fromJson(text, SkiSession::class.java)
                s to f
            }.getOrNull()
        }.sortedBy { it.first.startAtMillis }

        val toDelete = sessionsWithFile.take(sessionsWithFile.size - max)
        toDelete.forEach { (_, file) ->
            runCatching { file.delete() }
        }
    }
}
