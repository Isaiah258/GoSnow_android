package com.gosnow.app.recording.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SkiSession(
    val id: String = UUID.randomUUID().toString(),
    val startAtMillis: Long,
    val endAtMillis: Long,
    val durationSec: Int,
    val distanceKm: Double,
    val topSpeedKmh: Double,
    val avgSpeedKmh: Double,
    val verticalDropM: Int,
    val resortId: Long? = null    // 后面如果要接雪场 ID，可以用
)
