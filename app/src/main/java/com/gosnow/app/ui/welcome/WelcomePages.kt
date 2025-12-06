package com.gosnow.app.ui.welcome

import  androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.QueryStats

// -------- Welcome Page 1 --------

@Composable
fun WelcomePage1(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center          // ✅ 关键：整体往中间靠
    ) {
        // 不要再加顶部 Spacer 了

        Text(
            text = "欢迎使用上雪",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "上雪是一款专注滑雪场景的工具。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BannerRow(
                icon = Icons.Outlined.AutoAwesome,
                iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                title = "轻量上手，回归工具本质",
                subtitle = "即开即用，没有复杂的流程和冗余内容。"
            )

            BannerRow(
                icon = Icons.Outlined.QueryStats,
                iconBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                title = "数据记录，一目了然",
                subtitle = "用数字和图表展现你的生涯数据。"
            )

            BannerRow(
                icon = Icons.Outlined.Map,
                iconBgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                title = "雪场雪况，一站浏览",
                subtitle = "结合雪友分享与基础信息，帮你快速选好雪场。"
            )
        }
    }
}




// -------- Welcome Page 2 --------

@Composable
fun WelcomePage2(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center          // ✅ 同样居中
    ) {
        Text(
            text = "雪场的一天，都能用得上",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "从出发到收板，上雪帮你解决更多细节。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BannerRow(
                icon = Icons.Outlined.Search,
                iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                title = "失物招领，更快找回",
                subtitle = "在雪场发现或丢失物品时，快速发布与查找信息。"
            )

            BannerRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                iconBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                title = "雪圈社区，分享雪季生活",
                subtitle = "和雪友交流雪况、装备与出行经验。"
            )

            BannerRow(
                icon = Icons.Outlined.DirectionsCar,
                iconBgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                title = "顺风车，目的地匹配",
                subtitle = "车找人、人找车"
            )
        }
    }
}




// -------- Welcome Page 3 --------

@Composable
fun WelcomePage3(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center          // ✅ 也居中
    ) {
        Text(
            text = "上雪，和你一起完善",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "你的一次补充，能帮到很多雪友。现在就开始吧！",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BannerRow(
                icon = Icons.Outlined.AcUnit,
                iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                title = "一起把滑雪信息做对、做全",
                subtitle = "开放更新、真实分享、共同完善。"
            )

            BannerRow(
                icon = Icons.Outlined.AutoAwesome,
                iconBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                title = "持续更新 · 更多精彩",
                subtitle = "功能会不断上线，欢迎提出建议与反馈。"
            )

            BannerRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                iconBgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                title = "倾听体验，不断改进",
                subtitle = "我们会根据雪友的真实使用感受持续优化细节。"
            )
        }
    }
}




// -------- 复用的小行组件：图标 + 文本 --------

@Composable
private fun FeatureRow(
    icon: ImageVector,
    iconTint: Color,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(   // 放大一号
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
    }
}


// -------- 整个 Welcome Flow 容器（3 页，底部指示点 + 按钮） --------

@Composable
fun WelcomeFlowScreenWithIcons(
    onFinished: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pageCount = 3

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 上方内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (page) {
                0 -> WelcomePage1()
                1 -> WelcomePage2()
                2 -> WelcomePage3()
            }
        }

        // 下方指示点 + 按钮
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 先把内容抬离系统导航栏
                .navigationBarsPadding()
                // 再额外加一点 bottom padding，让按钮整体更往上
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 32.dp   // 原来是 vertical = 16.dp，这里加大 bottom
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 页指示点
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val selected = index == page
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (selected) 18.dp else 8.dp)
                            .background(
                                color = if (selected)
                                    MaterialTheme.colorScheme.onBackground
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 主按钮：改成黑色，并稍微抬高
            Button(
                onClick = {
                    if (page < pageCount - 1) {
                        page += 1
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,   // 黑色按钮
                    contentColor = Color.White      // 白色文字
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = if (page < pageCount - 1) "下一步" else "开始使用"
                )
            }
        }
    }
}

@Composable
private fun BannerRow(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形图标背景
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBgColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

