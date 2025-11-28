package com.gosnow.app.recording

import com.gosnow.app.recording.model.SkiSession

interface SessionRecorder {
    val currentSpeedKmh: Double
    val distanceKm: Double
    val topSpeedKmh: Double
    val verticalDropM: Int
    val state: RecordingState

    fun start()
    fun stop(): SkiSession?
}


