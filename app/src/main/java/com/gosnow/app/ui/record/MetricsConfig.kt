package com.gosnow.app.recording.metrics

data class MetricsConfig(
    // 精度过滤
    val maxHorizontalAccuracyM: Float = 30f,       // >30m 判为不可靠
    val maxSpeedKmh: Double = 120.0,              // >120 km/h 视为异常
    val minDtSec: Double = 0.2,                   // 相邻点最小时间间隔

    // 平滑
    val medianWindow: Int = 5,                    // 3~5 比较合适
    val lowPassAlpha: Double = 0.80,              // 越大越平滑

    // speed vs distance 一致性校验
    val consistencyTolerance: Double = 0.35,      // 相对误差>35% 视为不一致
    val clampOvershootRatio: Double = 1.5,        // 距离上限倍数

    // 距离累加阈值
    val minSpeedForDistanceKmh: Double = 0.8,     // 低于此认为原地抖动

    // 落差（高度变化）阈值
    val minVerticalChangeM: Double = 2.0          // 小于2m视为噪声
)


