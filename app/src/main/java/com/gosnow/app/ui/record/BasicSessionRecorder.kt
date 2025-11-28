package com.gosnow.app.ui.record

// 不要再 import LocationService / SamplingMode，跟它们同一个包就行
// 如果文件里有类似：
// import com.gosnow.app.ui.record.LocationManager.LocationService
// import com.gosnow.app.recording.location.LocationService
// 这些统统删掉

import com.gosnow.app.recording.metrics.BasicMetricsComputer
import com.gosnow.app.recording.model.SkiSession
import com.gosnow.app.recording.SessionRecorder
import com.gosnow.app.recording.RecordingState
import com.gosnow.app.recording.location.LocationService
import com.gosnow.app.recording.location.SamplingMode

class BasicSessionRecorder(
    private val locationService: LocationService,
    private val metrics: BasicMetricsComputer
) : SessionRecorder {

    private var _state: RecordingState = RecordingState.Idle
    override val state: RecordingState get() = _state

    override val currentSpeedKmh: Double
        get() = metrics.currentSpeedKmh

    override val distanceKm: Double
        get() = metrics.distanceKm

    override val topSpeedKmh: Double
        get() = metrics.topSpeedKmh

    override val verticalDropM: Int
        get() = metrics.verticalDropM

    private var sessionStartMillis: Long = 0L

    init {
        // 这里就能正常访问 onLocationSample 了
        locationService.onLocationSample = { loc ->
            metrics.consumeSample(loc)
        }
    }

    override fun start() {
        if (_state != RecordingState.Idle) return
        metrics.reset()
        sessionStartMillis = System.currentTimeMillis()
        locationService.setSamplingMode(SamplingMode.Active)
        locationService.start()
        _state = RecordingState.Recording
    }

    override fun stop(): SkiSession? {
        if (_state != RecordingState.Recording) return null

        locationService.stop()
        val end = System.currentTimeMillis()
        _state = RecordingState.Idle

        val durationSec = ((end - sessionStartMillis) / 1000L).toInt().coerceAtLeast(0)
        val distance = metrics.distanceKm
        val topSpeed = metrics.topSpeedKmh
        val vertical = metrics.verticalDropM
        val avgSpeed = if (durationSec > 0) {
            distance / (durationSec / 3600.0)
        } else {
            0.0
        }

        return SkiSession(
            startAtMillis = sessionStartMillis,
            endAtMillis = end,
            durationSec = durationSec,
            distanceKm = distance,
            topSpeedKmh = topSpeed,
            avgSpeedKmh = avgSpeed,
            verticalDropM = vertical
        )
    }
}
