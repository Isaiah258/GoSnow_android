package com.gosnow.app.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosnow.app.recording.model.SessionSummary
import com.gosnow.app.ui.record.storage.FileSessionStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecordingViewModel(
    private val recorder: SessionRecorder,
    private val store: FileSessionStore
) : ViewModel() {

    var isRecording by mutableStateOf(false)
        private set

    var durationText by mutableStateOf("00:00:00")
        private set

    var distanceKm by mutableStateOf(0.0)
        private set

    var maxSpeedKmh by mutableStateOf(0.0)
        private set

    var verticalDropM by mutableStateOf(0)
        private set

    private var timerJob: Job? = null
    private var sessionStartMillis: Long? = null

    /**
     * UI 调用：点击“开始记录 / 结束记录”。
     */
    fun onToggleRecording() {
        if (!isRecording) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun startRecording() {
        recorder.start()
        sessionStartMillis = System.currentTimeMillis()
        isRecording = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isRecording) {
                updateFromRecorder()
                delay(1000L)
            }
        }
    }

    private fun stopRecording() {
        val session = recorder.stop()
        isRecording = false
        timerJob?.cancel()
        timerJob = null

        sessionStartMillis = null

        session?.let { s ->
            // 最终再刷新一次 UI
            distanceKm = s.distanceKm
            maxSpeedKmh = s.topSpeedKmh
            verticalDropM = s.verticalDropM
            durationText = formatDuration(s.durationSec)

            // 异步存盘
            viewModelScope.launch {
                store.saveSession(s)
            }
        }
    }

    private fun updateFromRecorder() {
        // 时间用「现在 - start」保证和 iOS 一样：从点击开始起计时
        val start = sessionStartMillis
        val nowSec = if (start != null) {
            ((System.currentTimeMillis() - start) / 1000L).toInt().coerceAtLeast(0)
        } else {
            0
        }

        durationText = formatDuration(nowSec)
        distanceKm = recorder.distanceKm
        maxSpeedKmh = recorder.topSpeedKmh
        verticalDropM = recorder.verticalDropM
    }

    private fun formatDuration(sec: Int): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    fun buildSummary(): SessionSummary? {
        // 如果你之后想展示一个 Summary Sheet，可以直接用这个
        if (sessionStartMillis != null || !isRecording) {
            // 这里只是示意，实际你可能要从最近一次 session 读
        }
        return null
    }

    companion object {
        fun provideFactory(
            recorder: SessionRecorder,
            store: FileSessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RecordingViewModel(recorder, store) as T
            }
        }
    }
}


