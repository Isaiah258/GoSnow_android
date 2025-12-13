package com.gosnow.app.ui.discover

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosnow.app.datasupabase.CarpoolItem
import com.gosnow.app.datasupabase.LostAndFoundItem
import com.gosnow.app.datasupabase.LostFoundType
import com.gosnow.app.datasupabase.ResortRef
import com.gosnow.app.datasupabase.RoommateItem
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

/* =========================
 * Discover 首页入口
 * ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onLostAndFoundClick: () -> Unit,
    onCarpoolClick: () -> Unit,
    onRoommateClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "发现", style = MaterialTheme.typography.titleLarge) }
            )
        },
        containerColor = Color(0xFFF5F5F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DiscoverEntryCard(
                title = "失物招领",
                subtitle = "丢失？捡到？这里快速对接",
                icon = Icons.Default.Search,
                color = Color(0xFF5C6BC0),
                onClick = onLostAndFoundClick
            )
            DiscoverEntryCard(
                title = "顺风车",
                subtitle = "按雪场 / 日期找同行",
                icon = Icons.Default.DirectionsCar,
                color = Color(0xFF26A69A),
                onClick = onCarpoolClick
            )
            DiscoverEntryCard(
                title = "拼房合租",
                subtitle = "寻找合租雪友",
                icon = Icons.Default.Home,
                color = Color(0xFFF57C00),
                onClick = onRoommateClick
            )
        }
    }
}

@Composable
fun DiscoverEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* =========================
 * 失物招领
 * ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostAndFoundScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyLostAndFoundClick: () -> Unit,
    vm: LostAndFoundVM = viewModel(factory = LostAndFoundVM.factory())
) {
    val resorts by vm.resorts.collectAsStateWithLifecycle()
    val selectedResort by vm.selectedResort.collectAsStateWithLifecycle()
    val selectedDate by vm.selectedDate.collectAsStateWithLifecycle()
    val keyword by vm.keyword.collectAsStateWithLifecycle()
    val state by vm.publicState.collectAsStateWithLifecycle()

    var showResortPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    state.error?.let { err ->
        // 简单：每次错误出现就提示一次（你也可以做“只提示一次”的事件封装）
        scope.launch { snackbarHostState.showSnackbar(err) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("失物招领") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onMyLostAndFoundClick) {
                        Icon(Icons.Default.Article, contentDescription = "我的失物")
                    }
                    IconButton(onClick = onPublishClick) {
                        Icon(Icons.Default.Add, contentDescription = "发布")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showResortPicker = true },
                    label = { Text(selectedResort?.name ?: "所有雪场") },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(selectedDate?.formatShort() ?: "日期") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = keyword,
                onValueChange = { vm.setKeyword(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索物品关键词") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading && state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("加载中...")
                    }
                }
                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("没有找到相关物品", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.items, key = { it.id }) { item ->
                            LostAndFoundCard(item = item)
                        }
                        if (!state.endReached) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { vm.loadMorePublic() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) { Text(if (state.isLoading) "加载中..." else "加载更多") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = resorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                vm.selectResort(it)
                showResortPicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    vm.selectDate(millis?.toLocalDate())
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.selectDate(null)
                    showDatePicker = false
                }) { Text("清除") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun LostAndFoundCard(item: LostAndFoundItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(if (item.type == LostFoundType.LOST) "丢失" else "拾到") }
                )
                item.resort?.let {
                    FilterChip(selected = false, onClick = {}, label = { Text(it.name) })
                }
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(item.createdAt.formatShortDate()) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text = item.contact, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostAndFoundPublishScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit,
    vm: LostAndFoundVM = viewModel(factory = LostAndFoundVM.factory())
) {
    val resorts by vm.resorts.collectAsStateWithLifecycle()

    var selectedType by rememberSaveable { mutableStateOf(LostFoundType.LOST) }
    var description by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isValid = description.isNotBlank() && contact.isNotBlank() && selectedResort != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布物品") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selectedType == LostFoundType.LOST,
                    onClick = { selectedType = LostFoundType.LOST },
                    label = { Text("丢失") }
                )
                FilterChip(
                    selected = selectedType == LostFoundType.FOUND,
                    onClick = { selectedType = LostFoundType.FOUND },
                    label = { Text("捡到") }
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("物品描述") },
                minLines = 3
            )

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("联系方式 (微信/电话)") },
                leadingIcon = { Icon(Icons.Default.Textsms, contentDescription = null) }
            )

            OutlinedButton(
                onClick = { showResortPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(text = selectedResort?.name ?: "选择雪场")
            }

            Button(
                onClick = {
                    scope.launch {
                        val result = vm.publish(
                            type = selectedType,
                            description = description,
                            contact = contact,
                            resortId = selectedResort?.id
                        )
                        result.onSuccess {
                            snackbarHostState.showSnackbar("发布成功")
                            onPublished()
                        }.onFailure {
                            snackbarHostState.showSnackbar(it.message ?: "发布失败")
                        }
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("发布")
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = resorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                selectedResort = it
                showResortPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLostAndFoundScreen(
    onBackClick: () -> Unit,
    vm: LostAndFoundVM = viewModel(factory = LostAndFoundVM.factory())
) {
    val state by vm.myState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var deleteTarget by remember { mutableStateOf<LostAndFoundItem?>(null) }

    // 进入页面时拉一次
    androidx.compose.runtime.LaunchedEffect(Unit) { vm.refreshMy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的失物招领") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中...")
                }
            }
            state.items.isEmpty() -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "暂无失物招领记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(item.resort?.name ?: "未知雪场", style = MaterialTheme.typography.titleMedium)
                                    Text(item.createdAt.formatShortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(item.description, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Text(item.contact, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(onClick = { deleteTarget = item }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    if (!state.endReached) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { vm.loadMorePublic() }) {
                                    Text(if (state.isLoading) "加载中..." else "加载更多")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除帖子") },
            text = { Text("删除后无法恢复，确认删除这条失物招领信息吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val result = vm.deleteMy(target.id)
                        result.onSuccess {
                            snackbarHostState.showSnackbar("已删除")
                        }.onFailure {
                            snackbarHostState.showSnackbar(it.message ?: "删除失败")
                        }
                        deleteTarget = null
                    }
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }
}

/* =========================
 * 顺风车
 * ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarpoolScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyCarpoolClick: () -> Unit,
    vm: CarpoolVM = viewModel(factory = CarpoolVM.factory())
) {
    val resorts by vm.resorts.collectAsStateWithLifecycle()
    val selectedResort by vm.selectedResort.collectAsStateWithLifecycle()
    val selectedDate by vm.selectedDate.collectAsStateWithLifecycle()
    val state by vm.publicState.collectAsStateWithLifecycle()

    var showResortPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("顺风车") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onMyCarpoolClick) {
                        Icon(Icons.Default.Article, contentDescription = "我的顺风车")
                    }
                    IconButton(onClick = onPublishClick) {
                        Icon(Icons.Default.Add, contentDescription = "发布")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showResortPicker = true },
                    label = { Text(selectedResort?.name ?: "所有雪场") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(selectedDate?.formatShort() ?: "日期") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading && state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("加载中...") }
                }
                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error ?: "暂无顺风车信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.items, key = { it.id }) { item ->
                            CarpoolCard(item = item)
                        }
                        if (!state.endReached) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { vm.loadMorePublic() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) { Text(if (state.isLoading) "加载中..." else "加载更多") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = resorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                vm.selectResort(it)
                showResortPicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    vm.selectDate(millis?.toLocalDate())
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.selectDate(null)
                    showDatePicker = false
                }) { Text("清除") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun CarpoolCard(item: CarpoolItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(item.departAt.formatFull(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF5C6BC0))
                Text("出发地：${item.origin}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF26A69A))
                Text("目的地：${item.resort?.name ?: "未知雪场"}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(item.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarpoolPublishScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit,
    vm: CarpoolVM = viewModel(factory = CarpoolVM.factory())
) {
    val resorts by vm.resorts.collectAsStateWithLifecycle()

    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var selectedTime by rememberSaveable { mutableStateOf(LocalTime.of(8, 30)) }
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var origin by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isValid = origin.isNotBlank() && note.isNotBlank() && selectedResort != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布顺风车") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(selectedDate.formatFullDate())
                }
            }
            item {
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(selectedTime.formatTime())
                }
            }
            item {
                OutlinedButton(
                    onClick = { showResortPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(selectedResort?.name ?: "选择目的地雪场")
                }
            }
            item {
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("出发地") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注/联系方式") },
                    leadingIcon = { Icon(Icons.Default.Textsms, contentDescription = null) },
                    minLines = 3
                )
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            val r = vm.publish(
                                selectedDate = selectedDate,
                                selectedTime = selectedTime,
                                resortId = selectedResort!!.id,
                                origin = origin,
                                note = note
                            )
                            r.onSuccess {
                                snackbarHostState.showSnackbar("发布成功")
                                onPublished()
                            }.onFailure {
                                snackbarHostState.showSnackbar(it.message ?: "发布失败")
                            }
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) { Text("发布") }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = resorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                selectedResort = it
                showResortPicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDate()?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) { TimePicker(state = timePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCarpoolScreen(
    onBackClick: () -> Unit,
    vm: CarpoolVM = viewModel(factory = CarpoolVM.factory())
) {
    val state by vm.myState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<CarpoolItem?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) { vm.refreshMy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的顺风车") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中...")
                }
            }
            state.items.isEmpty() -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "暂无顺风车记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.departAt.formatFull(), style = MaterialTheme.typography.titleMedium)
                                    Text(item.resort?.name ?: "未知雪场", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(item.note, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { deleteTarget = item }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (!state.endReached) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { vm.loadMorePublic() }) {
                                    Text(if (state.isLoading) "加载中..." else "加载更多")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除帖子") },
            text = { Text("删除后无法恢复，确认删除这条顺风车信息吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val r = vm.deleteMy(target.id)
                        r.onSuccess { snackbarHostState.showSnackbar("已删除") }
                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "删除失败") }
                        deleteTarget = null
                    }
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } }
        )
    }
}

/* =========================
 * 拼房合租
 * ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoommateScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyRoommateClick: () -> Unit,
    vm: RoommateVM = viewModel(factory = RoommateVM.factory())
) {
    val resorts by vm.resorts.collectAsStateWithLifecycle()
    val selectedResort by vm.selectedResort.collectAsStateWithLifecycle()
    val state by vm.publicState.collectAsStateWithLifecycle()

    var showResortPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拼房合租") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                },
                actions = {
                    IconButton(onClick = onMyRoommateClick) { Icon(Icons.Default.ListAlt, contentDescription = "我的拼房") }
                    IconButton(onClick = onPublishClick) { Icon(Icons.Default.Add, contentDescription = "发布") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F7))
                .padding(16.dp)
        ) {
            AssistChip(
                onClick = { showResortPicker = true },
                label = { Text(selectedResort?.name ?: "所有雪场") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading && state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("加载中...") }
                }
                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error ?: "暂无拼房信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.items, key = { it.id }) { item ->
                            RoommateCard(item = item)
                        }
                        if (!state.endReached) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { vm.loadMorePublic() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) { Text(if (state.isLoading) "加载中..." else "加载更多") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = resorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                vm.selectResort(it)
                showResortPicker = false
            }
        )
    }
}

@Composable
fun RoommateCard(item: RoommateItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(item.resort?.name ?: "未知雪场", style = MaterialTheme.typography.titleMedium)
                Text(item.createdAt.formatShortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(item.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoommatePublishScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit,
    vm: RoommateVM = viewModel(factory = RoommateVM.factory())
) {
    val resorts by vm.resorts.collectAsStateWithLifecycle()

    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    var content by rememberSaveable { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isValid = content.isNotBlank() && selectedResort != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布拼房合租") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showResortPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(selectedResort?.name ?: "选择雪场")
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("拼房内容") },
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Text("平台仅提供信息展示，线下交易请注意安全。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)

            Button(
                onClick = {
                    scope.launch {
                        val r = vm.publish(resortId = selectedResort!!.id, content = content)
                        r.onSuccess {
                            snackbarHostState.showSnackbar("发布成功")
                            onPublished()
                        }.onFailure {
                            snackbarHostState.showSnackbar(it.message ?: "发布失败")
                        }
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) { Text("发布") }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = resorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                selectedResort = it
                showResortPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRoommateScreen(
    onBackClick: () -> Unit,
    vm: RoommateVM = viewModel(factory = RoommateVM.factory())
) {
    val state by vm.myState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<RoommateItem?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) { vm.refreshMy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的拼房") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("加载中...") }
            }
            state.items.isEmpty() -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "暂无拼房记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(item.resort?.name ?: "未知雪场", style = MaterialTheme.typography.titleMedium)
                                    Text(item.createdAt.formatShortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(item.content, maxLines = 3, overflow = TextOverflow.Ellipsis)

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(onClick = { deleteTarget = item }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    if (!state.endReached) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { vm.loadMorePublic() }) {
                                    Text(if (state.isLoading) "加载中..." else "加载更多")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除帖子") },
            text = { Text("删除后无法恢复，确认删除这条拼房信息吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val r = vm.deleteMy(target.id)
                        r.onSuccess { snackbarHostState.showSnackbar("已删除") }
                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "删除失败") }
                        deleteTarget = null
                    }
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } }
        )
    }
}

/* =========================
 * 公共组件
 * ========================= */
@Composable
fun ResortPickerDialog(
    allResorts: List<ResortRef>,
    selectedResort: ResortRef?,
    onDismissRequest: () -> Unit,
    onResortSelected: (ResortRef?) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = allResorts.filter { it.name.contains(query, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索雪场") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                LazyColumn(modifier = Modifier.height(260.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    onResortSelected(null)
                                    onDismissRequest()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("所有雪场", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    items(filtered) { resort ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    onResortSelected(resort)
                                    onDismissRequest()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isSelected = resort.id == selectedResort?.id
                            val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    resort.name,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = onConfirm) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("取消") } },
        text = content
    )
}

/* =========================
 * 工具方法
 * ========================= */
private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.formatShort(): String =
    DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault()).format(this)

private fun LocalDate.formatFullDate(): String =
    DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.getDefault()).format(this)

private fun LocalTime.formatTime(): String =
    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).format(this)

private fun LocalDateTime.formatShortDate(): String =
    DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.getDefault()).format(this)

private fun LocalDateTime.formatFull(): String =
    DateTimeFormatter.ofPattern("MM月dd日 HH:mm", Locale.getDefault()).format(this)
