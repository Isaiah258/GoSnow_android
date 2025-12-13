package com.gosnow.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

// 和 SwiftUI 对齐的概念
enum class StatsScope { WEEK, MONTH, SEASON }

// 只保留两个指标：滑行时间 / 滑行里程
enum class StatsMetric { DURATION, DISTANCE }

// 单个点（X 轴标签 + 数值）
data class StatsPoint(
    val label: String,
    val value: Float
)

// 概览数据（去掉雪天数）
data class StatsSummary(
    val totalDurationMin: Int,
    val totalDistanceKm: Float,
    val sessionsCount: Int
)

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vm: StatsViewModel = viewModel(factory = StatsViewModel.provideFactory(context))
    val ui by vm.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "活动",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        ScopeSegmentedRow(
            scope = ui.scope,
            onScopeChange = { vm.setScope(it) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SimpleBarChart(
            points = ui.points,
            metric = ui.metric,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 8.dp)
        )

        SummaryRow(
            summary = ui.summary,
            metric = ui.metric,
            onMetricChange = { vm.setMetric(it) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "记录次数",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${ui.summary.sessionsCount} 次",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
/* ---------------- 顶部「周 / 月 / 雪季」选择 ---------------- */

@Composable
private fun ScopeSegmentedRow(
    scope: StatsScope,
    onScopeChange: (StatsScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ScopeChip(
            text = "周",
            value = StatsScope.WEEK,
            selected = scope == StatsScope.WEEK,
            onScopeChange = onScopeChange,
            modifier = Modifier.weight(1f)
        )
        ScopeChip(
            text = "月",
            value = StatsScope.MONTH,
            selected = scope == StatsScope.MONTH,
            onScopeChange = onScopeChange,
            modifier = Modifier.weight(1f)
        )
        ScopeChip(
            text = "雪季",
            value = StatsScope.SEASON,
            selected = scope == StatsScope.SEASON,
            onScopeChange = onScopeChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScopeChip(
    text: String,
    value: StatsScope,
    selected: Boolean,
    onScopeChange: (StatsScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedBg = MaterialTheme.colorScheme.primary
    val selectedFg = MaterialTheme.colorScheme.onPrimary
    val unselectedFg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) selectedBg else Color.Transparent)
            .clickable { onScopeChange(value) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) selectedFg else unselectedFg,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

/* ---------------- 柱状图（滑行时间 / 滑行里程用） ---------------- */

@Composable
fun SimpleBarChart(
    points: List<StatsPoint>,
    metric: StatsMetric,
    modifier: Modifier = Modifier
) {
    val barColor = when (metric) {
        StatsMetric.DURATION -> Color(0xFFFF9800)   // 橙色：滑行时间
        StatsMetric.DISTANCE -> Color(0xFF4CAF50)   // 绿色：滑行里程
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Y 轴单位
        Text(
            text = when (metric) {
                StatsMetric.DURATION -> "单位：小时"
                StatsMetric.DISTANCE -> "单位：公里"
            },
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(start = 16.dp)
        )

        if (points.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            return
        }

        // 自适应最大值，但给一个下限，避免全是很小的数时柱子太夸张
        val minMax = when (metric) {
            StatsMetric.DURATION -> 0.5f   // 至少按 0.5 小时来算
            StatsMetric.DISTANCE -> 1f     // 至少 1km
        }
        val max = points.maxOfOrNull { it.value }?.coerceAtLeast(minMax) ?: minMax

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            points.forEach { p ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 上半部分：柱子 + 数值
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (p.value > 0f) {
                            // 自适应比例 + 留一点顶部空隙（最多 85% 高度）
                            val rawFraction = p.value / max
                            val fraction = (rawFraction.coerceIn(0f, 1f)) * 0.85f

                            // 柱子
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(barColor)
                            )

                            // 数值
                            val valueText = when (metric) {
                                StatsMetric.DURATION ->
                                    String.format("%.1f", p.value)   // 小时，一位小数
                                StatsMetric.DISTANCE ->
                                    p.value.toInt().toString()       // km
                            }

                            Text(
                                text = valueText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(bottom = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 底部：X 轴标签
                    Text(
                        text = p.label,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* ---------------- 底部两张 Summary 卡片 ---------------- */

@Composable
private fun SummaryRow(
    summary: StatsSummary,
    metric: StatsMetric,
    onMetricChange: (StatsMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "滑行时间",
            value = formatDuration(summary.totalDurationMin),
            selected = metric == StatsMetric.DURATION,
            color = Color(0xFFFF9800),
            onClick = { onMetricChange(StatsMetric.DURATION) },
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "滑行里程",
            value = String.format("%.1f km", summary.totalDistanceKm),
            selected = metric == StatsMetric.DISTANCE,
            color = Color(0xFF4CAF50),
            onClick = { onMetricChange(StatsMetric.DISTANCE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) color else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 84.dp)
            .shadow(if (selected) 10.dp else 0.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = bg
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = fg.copy(alpha = if (selected) 0.95f else 0.75f)
            )
        }
    }
}

/* ---------------- 小工具：分钟 -> “x小时y分” ---------------- */

private fun formatDuration(totalMin: Int): String {
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) {
        "${h}小时${m}分"
    } else {
        "$m 分钟"
    }
}

/* ---------------- 预览 ---------------- */

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    MaterialTheme {
        StatsScreen()
    }
}
