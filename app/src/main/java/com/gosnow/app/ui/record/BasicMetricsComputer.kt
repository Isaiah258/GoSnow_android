package com.gosnow.app.recording.metrics

import android.location.Location
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 只负责把一串 Location 样本转成：
 * 距离 / 当前速度 / 最高速度 / 累计落差
 *
 * 时间（duration）由上层用 start/stop 的时间差计算，
 * 这样行为和你 iOS“点开始就计时”是一致的。
 */
class BasicMetricsComputer(
    private val cfg: MetricsConfig = MetricsConfig()
) {
    var distanceKm: Double = 0.0
        private set

    var currentSpeedKmh: Double = 0.0
        private set

    var topSpeedKmh: Double = 0.0
        private set

    var verticalDropM: Int = 0
        private set

    private var verticalDropAccum: Double = 0.0

    private var lastLocation: Location? = null
    private var lastSmoothSpeed: Double = 0.0
    private val speedWindow: ArrayDeque<Double> = ArrayDeque()

    private var lastAltitude: Double? = null

    fun reset() {
        distanceKm = 0.0
        currentSpeedKmh = 0.0
        topSpeedKmh = 0.0
        verticalDropM = 0
        verticalDropAccum = 0.0

        lastLocation = null
        lastSmoothSpeed = 0.0
        speedWindow.clear()
        lastAltitude = null
    }

    fun consumeSample(loc: Location) {
        // 1) 基础过滤：精度
        if (loc.hasAccuracy()) {
            if (loc.accuracy <= 0f || loc.accuracy > cfg.maxHorizontalAccuracyM) {
                return
            }
        }

        val prev = lastLocation
        var dt = 0.0
        if (prev != null) {
            dt = (loc.time - prev.time) / 1000.0
            if (dt < cfg.minDtSec) {
                // 采样太密，不必处理
                return
            }
        }

        // 2) 原始速度（m/s -> km/h）
        var rawSpeedKmh: Double? = null
        if (loc.hasSpeed()) {
            val v = loc.speed      // m/s
            if (!v.isNaN() && !v.isInfinite() && v >= 0f) {
                val kmh = v * 3.6
                if (kmh <= cfg.maxSpeedKmh) {
                    rawSpeedKmh = kmh.toDouble()
                }
            }
        }

        // 3) 距离 & 基于距离估算速度
        var deltaKm = 0.0
        var vDeltaKmh: Double? = null
        if (prev != null) {
            val dm = prev.distanceTo(loc).toDouble()  // m
            deltaKm = dm / 1000.0
            if (dt > 0) {
                vDeltaKmh = (deltaKm / (dt / 3600.0))
            }
        }

        // 4) 选一个观测速度用来平滑
        val observedSpeedKmh: Double = max(0.0, rawSpeedKmh ?: (vDeltaKmh ?: 0.0))

        // 5) 一致性校验：raw vs vDelta 差太大 → clamp 距离
        var distanceToAccumulateKm = deltaKm
        if (rawSpeedKmh != null && vDeltaKmh != null && vDeltaKmh > 0) {
            val relDiff = abs(rawSpeedKmh - vDeltaKmh) / vDeltaKmh
            if (relDiff > cfg.consistencyTolerance) {
                val trusted = min(rawSpeedKmh, vDeltaKmh)
                val maxAllowedKm = (trusted / 3600.0) * dt * cfg.clampOvershootRatio
                distanceToAccumulateKm = min(distanceToAccumulateKm, maxAllowedKm)
            }
        }

        // 6) 速度平滑：中值 + 低通
        val median = pushAndMedian(observedSpeedKmh)
        val smooth = lowPass(lastSmoothSpeed, median, cfg.lowPassAlpha)
        lastSmoothSpeed = smooth
        currentSpeedKmh = smooth

        // 7) 最高速用平滑后的值
        topSpeedKmh = max(topSpeedKmh, smooth)

        // 8) 累计落差：只记“向下”的高度变化
        val alt = loc.altitude
        if (!alt.isNaN() && !alt.isInfinite()) {
            val lastAlt = lastAltitude
            if (lastAlt != null) {
                val deltaAlt = alt - lastAlt
                if (deltaAlt < -cfg.minVerticalChangeM) {
                    // 向下超过阈值
                    verticalDropAccum += -deltaAlt
                    verticalDropM = verticalDropAccum.roundToInt()
                }
            }
            lastAltitude = alt
        }

        // 9) 距离累加：速度很低时不计距离，避免原地“走路”
        if (smooth >= cfg.minSpeedForDistanceKmh && dt > 0) {
            val maxBySmoothKm = (smooth / 3600.0) * dt * cfg.clampOvershootRatio
            distanceKm += min(distanceToAccumulateKm, maxBySmoothKm)
        }

        lastLocation = loc
    }

    // --- 辅助 ---

    private fun pushAndMedian(v: Double): Double {
        val w = max(3, cfg.medianWindow or 1) // 确保为奇数
        speedWindow.addLast(v)
        while (speedWindow.size > w) {
            speedWindow.removeFirst()
        }
        val sorted = speedWindow.sorted()
        return sorted[sorted.size / 2]
    }

    private fun lowPass(prev: Double, current: Double, alpha: Double): Double {
        return alpha * prev + (1 - alpha) * current
    }
}


