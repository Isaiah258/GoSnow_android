package com.gosnow.app.ui.feed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeedScreen(
    onCreatePostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholderItems = listOf(
        "雪圈列表占位 1",
        "雪圈列表占位 2",
        "雪圈列表占位 3"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "雪圈") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // TODO: 打开雪圈发帖编辑器
                onCreatePostClick()
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "发帖")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(placeholderItems) { item ->
                Text(
                    text = "$item — TODO: 接入雪圈帖子数据流",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
