package com.gosnow.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonChecked
// 其他 import 下面加：
import coil.compose.AsyncImage
import com.gosnow.app.datasupabase.CurrentUserStore
import com.gosnow.app.datasupabase.CurrentUserProfile


import androidx.compose.runtime.collectAsState

import kotlin.math.abs

/**
 * UI state for the Home screen.
 */
data class HomeUiState(
    val todayDistanceKm: Double = 0.0,
    val totalDistanceKm: Double = 0.0,
    val totalDurationHours: Double = 0.0,
    val daysOnSnow: Int = 0,
    val deltaVsYesterdayKm: Double? = null
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
            daysOnSnow = 18,
            deltaVsYesterdayKm = 2.3
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
    onFeatureClick: (String) -> Unit,        // 哪个功能交给外层决定
    onAvatarClick: () -> Unit,              // 点击右上角头像
    onBottomNavSelected: (BottomNavItem) -> Unit,
    currentRoute: String,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentProfile by CurrentUserStore.profile.collectAsState()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // ✅ 这里把 profile 传进去
            HeaderSection(
                profile = currentProfile,
                onAvatarClick = onAvatarClick
            )

            Spacer(modifier = Modifier.height(20.dp))
            TodayHeroCard(uiState)
            Spacer(modifier = Modifier.height(24.dp))
            LifetimeStatsSection(uiState)
            Spacer(modifier = Modifier.height(28.dp))
            FeaturedSection(onFeatureClick = onFeatureClick)
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryActionButton(onStartRecording = onStartRecording)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/* ------------------------ Header ------------------------ */
@Composable
private fun HeaderSection(
    profile: CurrentUserProfile?,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "25–26 雪季",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            ),
            color = Color(0xFF111111)
        )

        // 右上角头像：优先用远程头像，其次用昵称首字母，最后用 G
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF111111))
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                !profile?.avatarUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = profile!!.avatarUrl,
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                !profile?.userName.isNullOrBlank() -> {
                    val initial = profile!!.userName.first().uppercaseChar().toString()
                    Text(
                        text = initial,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                else -> {
                    Text(
                        text = "G",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ------------------------ Today Hero Card ------------------------ */

@Composable
private fun TodayHeroCard(uiState: HomeUiState) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日滑行",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111111)
                )

                uiState.deltaVsYesterdayKm?.let { delta ->
                    if (delta != 0.0) {
                        TodayDeltaPill(deltaKm = delta)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format("%.1f", uiState.todayDistanceKm),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 52.sp
                    ),
                    color = Color(0xFF111111)
                )
                Text(
                    text = "km",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF3A3A3C)
                )
            }
        }
    }
}

@Composable
private fun TodayDeltaPill(deltaKm: Double) {
    val isPositive = deltaKm > 0
    val bgColor = if (isPositive) Color(0xFFE6F9EA) else Color(0xFFFFF2E8)
    val textColor = if (isPositive) Color(0xFF1A7F37) else Color(0xFFC23B00)
    val label = if (isPositive) "比昨天多" else "比昨天少"
    val valueText = String.format("%.1f km", abs(deltaKm))

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isPositive) "▲" else "▼",
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
            Text(
                text = "$label $valueText",
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

/* ------------------------ Lifetime Stats ------------------------ */

@Composable
private fun LifetimeStatsSection(uiState: HomeUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "生涯数据",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF111111)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DistanceStatCard(
                title = "总里程",
                value = String.format("%.1f", uiState.totalDistanceKm),
                icon = Icons.Filled.DownhillSkiing,
                iconTint = Color(0xFF0A84FF),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "总时长",
                    value = String.format("%.1f 小时", uiState.totalDurationHours),
                    icon = Icons.Filled.AccessTime,
                    iconTint = Color(0xFFFF9F0A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                StatCard(
                    title = "在雪天数",
                    value = "${uiState.daysOnSnow} 天",
                    icon = Icons.Filled.AcUnit,
                    iconTint = Color(0xFF5E5CE6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DistanceStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6E6E73)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 34.sp
                ),
                color = Color(0xFF111111),
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Text(
                text = "km",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6E6E73),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6E6E73)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111111)
                )
            }
        }
    }
}

/* ------------------------ Featured section ------------------------ */

@Composable
private fun FeaturedSection(onFeatureClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "精选",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF111111),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(featureItems) { item ->
                FeatureTile(
                    item = item,
                    onClick = { onFeatureClick(item.title) }
                )
            }
        }
    }
}

private val featureItems = listOf(
    FeatureTileData(
        title = "滑行数据",
        subtitle = "周 / 月 / 雪季趋势图表",
        icon = Icons.Filled.BarChart
    ),
    FeatureTileData(
        title = "更多功能",
        subtitle = "更多功能赶来中",
        icon = Icons.Filled.Explore
    )
)

@Composable
private fun FeatureTile(item: FeatureTileData, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
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
                color = Color(0xFF111111),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111111)
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6E6E73),
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

/* ------------------------ Primary CTA button ------------------------ */

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
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111111),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "开始记录",
                modifier = Modifier.padding(vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/* ------------------------ Bottom nav (外层用) ------------------------ */

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

/* ------------------------ Preview ------------------------ */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    GosnowTheme {
        HomeScreen(
            onStartRecording = {},
            onFeatureClick = { _ -> },
            onAvatarClick = {},
            onBottomNavSelected = {},
            currentRoute = BottomNavItem.Record.route
        )
    }
}
