package com.gosnow.app.recording.model

data class SessionSummary(
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val topSpeedKmh: Double,
    val verticalDropM: Int,
    val durationSec: Int
) {
    val durationText: String
        get() {
            val h = durationSec / 3600
            val m = (durationSec % 3600) / 60
            val s = durationSec % 60
            return if (h > 0) {
                String.format("%d:%02d:%02d", h, m, s)
            } else {
                String.format("%02d:%02d", m, s)
            }
        }
}
