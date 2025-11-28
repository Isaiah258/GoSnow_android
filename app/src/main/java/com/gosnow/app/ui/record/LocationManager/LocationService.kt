package com.gosnow.app.recording.location


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat

// 采样模式：激活（滑行中）/ 空闲（坐缆车、休息）
enum class SamplingMode { Active, Idle }

/**
 * 抽象定位服务接口
 */
interface LocationService {
    // 每次有新 Location 就回调
    var onLocationSample: ((Location) -> Unit)?

    fun start()
    fun stop()
    fun setSamplingMode(mode: SamplingMode)
}

/**
 * 只用系统 LocationManager + GPS_PROVIDER，
 * 不依赖 Google Play，中国大陆机型都能用。
 */
class SystemLocationService(
    private val context: Context
) : LocationService {

    override var onLocationSample: ((Location) -> Unit)? = null

    private val lm: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var isStarted = false
    private val provider = LocationManager.GPS_PROVIDER

    private val listener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            onLocationSample?.invoke(location)
        }

        @Deprecated("Deprecated in API 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun start() {
        if (isStarted) return
        if (!hasLocationPermission()) return
        isStarted = true

        try {
            lm.requestLocationUpdates(
                provider,
                1000L,   // 1s
                5f,      // 5m
                listener,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            isStarted = false
        }
    }

    override fun stop() {
        if (!isStarted) return
        try {
            lm.removeUpdates(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isStarted = false
    }

    override fun setSamplingMode(mode: SamplingMode) {
        if (!isStarted) return
        if (!hasLocationPermission()) return

        val (minTime, minDistance) = when (mode) {
            SamplingMode.Active -> 1000L to 5f      // 1s / 5m
            SamplingMode.Idle   -> 5000L to 25f     // 5s / 25m
        }

        try {
            lm.removeUpdates(listener)
            lm.requestLocationUpdates(
                provider,
                minTime,
                minDistance,
                listener,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }
}
