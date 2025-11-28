package com.gosnow.app.recording.storage

import com.gosnow.app.recording.model.SkiSession

interface SessionStore {
    suspend fun saveSession(session: SkiSession)
    suspend fun loadSessions(): List<SkiSession>
}


