package com.gosnow.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosnow.app.ui.theme.GosnowTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state for the Home screen.
 */
data class HomeUiState(
    val todayDistanceKm: Double = 0.0,
    val totalDistanceKm: Double = 0.0,
    val totalDurationHours: Double = 0.0,
    val daysOnSnow: Int = 0
)

/**
 * Demo view model that exposes static data for the Home screen.
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            todayDistanceKm = 12.4,
            totalDistanceKm = 346.7,
            totalDurationHours = 58.2,
            daysOnSnow = 18
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Record : BottomNavItem("home", "记录", Icons.Filled.RadioButtonChecked)
    data object Community : BottomNavItem("feed", "雪圈", Icons.Filled.Groups)
    data object Discover : BottomNavItem("discover", "发现", Icons.Filled.Explore)
}

@Composable
fun HomeScreen(
    onStartRecording: () -> Unit,
    onFeatureClick: (String) -> Unit,
    onBottomNavSelected: (BottomNavItem) -> Unit,
    currentRoute: String,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navItems = listOf(
        BottomNavItem.Record,
        BottomNavItem.Community,
        BottomNavItem.Discover
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = onBottomNavSelected
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            HeaderSection()
            Spacer(modifier = Modifier.height(20.dp))
            TodaySummaryCard(uiState)
            Spacer(modifier = Modifier.height(20.dp))
            LifetimeStatsSection(uiState)
            Spacer(modifier = Modifier.height(28.dp))
            FeaturedSection(onFeatureClick = onFeatureClick)
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryActionButton(onStartRecording = onStartRecording)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "25–26 雪季",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "准备好开冲了吗？",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flare,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Text(
                text = "雪场天气晴朗，滑雪的绝佳时刻！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(uiState: HomeUiState) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "今日滑行",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", uiState.todayDistanceKm),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 52.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "km",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricChip(label = "状态", value = "呼吸顺畅 · 状态佳")
                MetricChip(label = "雪况", value = "粉雪 · 轻松滑行")
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.06f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LifetimeStatsSection(uiState: HomeUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "生涯数据",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "总里程",
                    value = String.format("%.1f km", uiState.totalDistanceKm),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "总时长",
                    value = String.format("%.1f 小时", uiState.totalDurationHours),
                    modifier = Modifier.weight(1f)
                )
            }
            StatCard(
                title = "在雪天数",
                value = "${uiState.daysOnSnow} 天",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeaturedSection(onFeatureClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "精选",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(featureItems) { item ->
                FeatureTile(item = item, onClick = { onFeatureClick(item.title) })
            }
        }
    }
}

private val featureItems = listOf(
    FeatureTileData(title = "滑行数据", subtitle = "周/月/雪季 趋势图表", icon = Icons.Filled.BarChart),
    FeatureTileData(title = "雪况投票", subtitle = "一起评价今日雪况", icon = Icons.Filled.Celebration),
    FeatureTileData(title = "更多功能", subtitle = "与朋友一起玩雪", icon = Icons.Filled.Explore)
)

@Composable
private fun FeatureTile(item: FeatureTileData, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.width(220.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class FeatureTileData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
private fun PrimaryActionButton(onStartRecording: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onStartRecording,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "开始记录",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemSelected: (BottomNavItem) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GosnowTheme {
        HomeScreen(
            onStartRecording = {},
            onFeatureClick = {},
            onBottomNavSelected = {},
            currentRoute = BottomNavItem.Record.route
        )
    }
}
