package com.gosnow.app.ui.record.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.pow

/**
 * 用气压计计算“相对高度”（以开始记录那一刻为 0m）
 * - p0 = 开始时的气压
 * - altitude = 44330 * (1 - (p/p0)^0.1903)
 *
 * 注意：这是“相对高度”，非常适合算累计落差/爬升，且比 GPS altitude 稳。
 */
class BarometerAltimeter(
    context: Context,
    private val smoothAlpha: Float = 0.85f // 越大越平滑（0.8~0.9 推荐）
) : SensorEventListener {

    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sm.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private var p0: Float? = null
    private var _altitudeM: Float? = null

    val altitudeM: Float?
        get() = _altitudeM

    fun isAvailable(): Boolean = pressureSensor != null

    fun start() {
        p0 = null
        _altitudeM = null
        pressureSensor?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_PRESSURE) return
        val p = event.values.firstOrNull() ?: return
        if (p <= 0f) return

        val base = p0 ?: run {
            p0 = p
            p
        }

        // 相对高度（m）
        val rawAlt = 44330f * (1f - (p / base).pow(0.1903f))

        val prev = _altitudeM
        _altitudeM = if (prev == null) {
            rawAlt
        } else {
            // 指数平滑（低通）
            smoothAlpha * prev + (1f - smoothAlpha) * rawAlt
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
