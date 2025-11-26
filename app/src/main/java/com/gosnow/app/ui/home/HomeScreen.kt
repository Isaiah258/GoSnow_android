package com.gosnow.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onNavigateToDiscoverLost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = "首页") },
            actions = {
                IconButton(onClick = onProfileClick) {
                    // TODO: 替换为真实头像数据并跳转到个人资料页
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "个人资料"
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "滑雪数据总览占位",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TODO: 根据 iOS 首页展示真实的滑雪统计与概览",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "常用功能入口",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TODO: 补充 iOS 首页常用入口（如约课、购票等）",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "示例入口：失物招领",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击跳转",
                            modifier = Modifier
                                .clickable { onNavigateToDiscoverLost() }
                                .padding(top = 4.dp)
                                .align(Alignment.End),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            items(listOf("首页模块占位 1", "首页模块占位 2")) { itemText ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "$itemText — TODO: 对齐 iOS 的实际板块",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
