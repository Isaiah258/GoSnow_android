package com.gosnow.app.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiscoverScreen(
    onLostAndFoundClick: () -> Unit,
    onFeatureClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val featureEntries = listOf(
        "雪具租赁（占位）",
        "教程/攻略（占位）"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = "发现") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLostAndFoundClick)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "失物招领",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TODO: 对接 iOS 发现页的失物招领列表",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            featureEntries.forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFeatureClick(entry) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TODO: 根据 iOS 发现页的实际入口完善跳转",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
