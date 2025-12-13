package com.gosnow.app.ui.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosnow.app.ui.record.storage.FileSessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class StatsUiState(
    val scope: StatsScope = StatsScope.WEEK,
    val metric: StatsMetric = StatsMetric.DURATION,
    val points: List<StatsPoint> = emptyList(),
    val summary: StatsSummary = StatsSummary(
        totalDurationMin = 0,
        totalDistanceKm = 0f,
        sessionsCount = 0
    )
)

class StatsViewModel(
    private val store: FileSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        refresh()
    }

    fun setScope(scope: StatsScope) {
        _uiState.update { it.copy(scope = scope) }
        refresh()
    }

    fun setMetric(metric: StatsMetric) {
        _uiState.update { it.copy(metric = metric) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val all = store.loadSessions()

            val zone = ZoneId.systemDefault()
            fun sessionDate(millis: Long): LocalDate =
                Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

            val today = LocalDate.now(zone)
            val scope = _uiState.value.scope
            val metric = _uiState.value.metric

            // 1) 先筛出当前 scope 的 sessions
            val scoped = when (scope) {
                StatsScope.WEEK -> {
                    val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val end = start.plusDays(7)
                    all.filter {
                        val d = sessionDate(it.startAtMillis)
                        !d.isBefore(start) && d.isBefore(end)
                    }
                }

                StatsScope.MONTH -> {
                    val start = today.withDayOfMonth(1)
                    val end = start.plusMonths(1)
                    all.filter {
                        val d = sessionDate(it.startAtMillis)
                        !d.isBefore(start) && d.isBefore(end)
                    }
                }

                StatsScope.SEASON -> {
                    // 雪季：11 月 ~ 次年 4 月
                    val seasonStartYear = if (today.monthValue >= 11) today.year else today.year - 1
                    val start = LocalDate.of(seasonStartYear, 11, 1)
                    val end = LocalDate.of(seasonStartYear + 1, 5, 1) // 5月1日为开区间
                    all.filter {
                        val d = sessionDate(it.startAtMillis)
                        !d.isBefore(start) && d.isBefore(end)
                    }
                }
            }

            fun metricValueHoursOrKm(durationSec: Int, distanceKm: Double): Float {
                return when (metric) {
                    StatsMetric.DURATION -> (durationSec.toFloat() / 3600f) // 小时
                    StatsMetric.DISTANCE -> distanceKm.toFloat()            // 公里
                }
            }

            // 2) 生成 points
            val points = when (scope) {
                StatsScope.WEEK -> {
                    val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val labels = listOf("一", "二", "三", "四", "五", "六", "日")
                    (0..6).map { i ->
                        val day = start.plusDays(i.toLong())
                        val v = scoped
                            .filter { sessionDate(it.startAtMillis) == day }
                            .sumOf { metricValueHoursOrKm(it.durationSec, it.distanceKm.toDouble()).toDouble() }
                            .toFloat()
                        StatsPoint(labels[i], v)
                    }
                }

                StatsScope.MONTH -> {
                    // 固定 4 段：1–7、8–14、15–21、22–月底
                    val ym = YearMonth.from(today)
                    val endDay = ym.lengthOfMonth()

                    fun bucket(day: Int): Int = when {
                        day in 1..7 -> 0
                        day in 8..14 -> 1
                        day in 15..21 -> 2
                        else -> 3
                    }

                    val sums = FloatArray(4)
                    scoped.forEach {
                        val d = sessionDate(it.startAtMillis)
                        val b = bucket(d.dayOfMonth)
                        sums[b] += metricValueHoursOrKm(it.durationSec, it.distanceKm.toDouble())
                    }

                    listOf(
                        StatsPoint("1-7日", sums[0]),
                        StatsPoint("8-14日", sums[1]),
                        StatsPoint("15-21日", sums[2]),
                        StatsPoint("22-月底", sums[3]),
                    )
                }

                StatsScope.SEASON -> {
                    val seasonStartYear = if (today.monthValue >= 11) today.year else today.year - 1
                    val months = listOf(
                        YearMonth.of(seasonStartYear, 11) to "11月",
                        YearMonth.of(seasonStartYear, 12) to "12月",
                        YearMonth.of(seasonStartYear + 1, 1) to "1月",
                        YearMonth.of(seasonStartYear + 1, 2) to "2月",
                        YearMonth.of(seasonStartYear + 1, 3) to "3月",
                        YearMonth.of(seasonStartYear + 1, 4) to "4月",
                    )

                    months.map { (ym, label) ->
                        val v = scoped
                            .filter { YearMonth.from(sessionDate(it.startAtMillis)) == ym }
                            .sumOf { metricValueHoursOrKm(it.durationSec, it.distanceKm.toDouble()).toDouble() }
                            .toFloat()
                        StatsPoint(label, v)
                    }
                }
            }

            // 3) Summary（跟随 scope）
            val totalDurationMin = scoped.sumOf { it.durationSec } / 60
            val totalDistanceKm = scoped.sumOf { it.distanceKm }.toFloat()
            val sessionsCount = scoped.size

            _uiState.update {
                it.copy(
                    points = points,
                    summary = StatsSummary(
                        totalDurationMin = totalDurationMin,
                        totalDistanceKm = totalDistanceKm,
                        sessionsCount = sessionsCount
                    )
                )
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsViewModel(
                        store = FileSessionStore(context.applicationContext)
                    ) as T
                }
            }
    }
}


