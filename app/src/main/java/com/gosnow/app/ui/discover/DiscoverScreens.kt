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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosnow.app.ui.theme.GosnowTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(
    onLostAndFoundClick: () -> Unit,
    onCarpoolClick: () -> Unit,
    onRoommateClick: () -> Unit
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostAndFoundScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val datePickerState = rememberDatePickerState()

    val filteredList = sampleLostAndFoundList.filter { item ->
        val matchResort = selectedResort?.id?.let { item.resort?.id == it } ?: true
        val matchDate = selectedDate?.let { item.createdAt.toLocalDate() == it } ?: true
        val matchSearch = searchQuery.isBlank() || item.description.contains(searchQuery, ignoreCase = true)
        matchResort && matchDate && matchSearch
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
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索物品关键词") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "没有找到相关物品", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList) { item ->
                        LostAndFoundCard(item = item)
                    }
                }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = sampleResorts,
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
                    val millis = datePickerState.selectedDateMillis
                    selectedDate = millis?.toLocalDate()
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedDate = null
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
                FilterChip(selected = false, onClick = {}, label = { Text(item.createdAt.formatShortDate()) })
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
    onPublished: () -> Unit
) {
    var selectedType by rememberSaveable { mutableStateOf(LostFoundType.LOST) }
    var description by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isValid = description.isNotBlank() && contact.isNotBlank()

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
                    label = { Text("拾到") }
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
                Text(text = selectedResort?.name ?: "选择雪场 (可选)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("发布成功（暂未接入后端）")
                        onPublished()
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
            allResorts = sampleResorts,
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
fun CarpoolScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyCarpoolClick: () -> Unit
) {
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val datePickerState = rememberDatePickerState()
    var isLoading by remember { mutableStateOf(false) }

    val filteredList = sampleCarpoolList.filter { item ->
        !item.isCanceled &&
            (selectedResort?.id?.let { item.resort?.id == it } ?: true) &&
            (selectedDate?.let { item.departAt.toLocalDate() == it } ?: true)
    }

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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中...")
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无顺风车信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList) { item ->
                        CarpoolCard(item = item)
                    }
                }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = sampleResorts,
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
                    val millis = datePickerState.selectedDateMillis
                    selectedDate = millis?.toLocalDate()
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedDate = null
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
                Text(text = item.departAt.formatFull(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF5C6BC0))
                Text(text = "出发地：${item.origin}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF26A69A))
                Text(text = "目的地：${item.resort?.name ?: "未知雪场"}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = item.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarpoolPublishScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit
) {
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
    val isValid = origin.isNotBlank() && note.isNotBlank()

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
                    Text(text = selectedDate.formatFullDate())
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
                    Text(text = selectedTime.formatTime())
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
                    Text(text = selectedResort?.name ?: "选择目的地雪场")
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
                            snackbarHostState.showSnackbar("发布成功（暂未接入后端）")
                            onPublished()
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
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = sampleResorts,
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
                    val millis = datePickerState.selectedDateMillis
                    millis?.toLocalDate()?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun MyCarpoolScreen(onBackClick: () -> Unit) {
    val myList = sampleCarpoolList
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的顺风车") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myList) { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = item.departAt.formatFull(), style = MaterialTheme.typography.titleMedium)
                            Text(text = item.resort?.name ?: "未知雪场", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = item.note, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        val statusColor = if (item.isCanceled) Color(0xFFEF5350) else Color(0xFF26A69A)
                        Text(text = if (item.isCanceled) "已取消" else "进行中", color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoommateScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    onMyRoommateClick: () -> Unit
) {
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    val filtered = sampleRoommateList.filter { item ->
        selectedResort?.id?.let { item.resort?.id == it } ?: true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拼房合租") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onMyRoommateClick) {
                        Icon(Icons.Default.ListAlt, contentDescription = "我的拼房")
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
                .padding(16.dp)
        ) {
            AssistChip(
                onClick = { showResortPicker = true },
                label = { Text(selectedResort?.name ?: "所有雪场") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无拼房信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered) { item ->
                        RoommateCard(item = item)
                    }
                }
            }
        }
    }

    if (showResortPicker) {
        ResortPickerDialog(
            allResorts = sampleResorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                selectedResort = it
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
                Text(text = item.resort?.name ?: "未知雪场", style = MaterialTheme.typography.titleMedium)
                Text(text = item.createdAt.formatShortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = item.content, style = MaterialTheme.typography.bodyMedium)
            val statusColor = if (item.isCanceled) Color(0xFFEF5350) else Color(0xFF26A69A)
            Text(text = if (item.isCanceled) "已取消" else "进行中", color = statusColor, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoommatePublishScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit
) {
    var selectedResort by rememberSaveable { mutableStateOf<ResortRef?>(null) }
    var showResortPicker by remember { mutableStateOf(false) }
    var content by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isValid = content.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布拼房合租") },
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
            OutlinedButton(
                onClick = { showResortPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(text = selectedResort?.name ?: "选择雪场")
            }
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("拼房内容 (必填)") },
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Text(
                text = "平台仅提供信息展示，线下交易请注意安全。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("发布成功（暂未接入后端）")
                        onPublished()
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
            allResorts = sampleResorts,
            selectedResort = selectedResort,
            onDismissRequest = { showResortPicker = false },
            onResortSelected = {
                selectedResort = it
                showResortPicker = false
            }
        )
    }
}

@Composable
fun MyRoommateScreen(onBackClick: () -> Unit) {
    val myList = sampleRoommateList
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的拼房") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myList) { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = item.resort?.name ?: "未知雪场", style = MaterialTheme.typography.titleMedium)
                            Text(text = item.createdAt.formatShortDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = item.content, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        val statusColor = if (item.isCanceled) Color(0xFFEF5350) else Color(0xFF26A69A)
                        Text(text = if (item.isCanceled) "已取消" else "进行中", color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
                            modifier = Modifier
                                .fillMaxWidth()
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
                            modifier = Modifier
                                .fillMaxWidth()
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
                                Text(resort.name, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
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
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("取消") }
        },
        text = content
    )
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.formatShort(): String = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault()).format(this)

private fun LocalDate.formatFullDate(): String = DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.getDefault()).format(this)

private fun LocalTime.formatTime(): String = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).format(this)

private fun LocalDateTime.formatShortDate(): String = DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.getDefault()).format(this)

private fun LocalDateTime.formatFull(): String = DateTimeFormatter.ofPattern("MM月dd日 HH:mm", Locale.getDefault()).format(this)

// region Preview
@Preview(showBackground = true)
@Composable
fun PreviewDiscoverScreen() {
    GosnowTheme {
        DiscoverScreen(onLostAndFoundClick = {}, onCarpoolClick = {}, onRoommateClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLostAndFoundScreen() {
    GosnowTheme {
        LostAndFoundScreen(onBackClick = {}, onPublishClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCarpoolScreen() {
    GosnowTheme {
        CarpoolScreen(onBackClick = {}, onPublishClick = {}, onMyCarpoolClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRoommateScreen() {
    GosnowTheme {
        RoommateScreen(onBackClick = {}, onPublishClick = {}, onMyRoommateClick = {})
    }
}
// endregion Preview

// region Data models & samples
enum class LostFoundType { LOST, FOUND }

data class LostAndFoundItem(
    val id: String,
    val resort: ResortRef?,
    val type: LostFoundType,
    val description: String,
    val contact: String,
    val createdAt: LocalDateTime
)

data class CarpoolItem(
    val id: String,
    val resort: ResortRef?,
    val departAt: LocalDateTime,
    val origin: String,
    val note: String,
    val isCanceled: Boolean
)

data class RoommateItem(
    val id: String,
    val resort: ResortRef?,
    val content: String,
    val createdAt: LocalDateTime,
    val isCanceled: Boolean
)

data class ResortRef(val id: Int, val name: String)

private val sampleResorts = listOf(
    ResortRef(1, "崇礼万龙"),
    ResortRef(2, "张家口云顶"),
    ResortRef(3, "吉林北大壶"),
    ResortRef(4, "长白山万达"),
    ResortRef(5, "阿勒泰将军山")
)

private val sampleLostAndFoundList = listOf(
    LostAndFoundItem(
        id = "lf1",
        resort = sampleResorts[0],
        type = LostFoundType.LOST,
        description = "黑色Smith雪镜，在中级道休息区丢失，有拾到的小伙伴联系我！",
        contact = "微信：snowlover88",
        createdAt = LocalDateTime.now().minusHours(3)
    ),
    LostAndFoundItem(
        id = "lf2",
        resort = sampleResorts[1],
        type = LostFoundType.FOUND,
        description = "捡到一副白色手套，品牌Burton，暂存在缆车口服务亭。",
        contact = "电话：18800001111",
        createdAt = LocalDateTime.now().minusDays(1)
    ),
    LostAndFoundItem(
        id = "lf3",
        resort = sampleResorts[2],
        type = LostFoundType.LOST,
        description = "红色滑雪杖一支，可能遗留在停车场附近。",
        contact = "微信：skifan",
        createdAt = LocalDateTime.now().minusHours(8)
    )
)

private val sampleCarpoolList = listOf(
    CarpoolItem(
        id = "cp1",
        resort = sampleResorts[0],
        departAt = LocalDateTime.now().plusDays(1).withHour(7).withMinute(30),
        origin = "北京望京地铁站",
        note = "拼车去万龙，车牌京A12345，微信：carpool77",
        isCanceled = false
    ),
    CarpoolItem(
        id = "cp2",
        resort = sampleResorts[1],
        departAt = LocalDateTime.now().plusDays(2).withHour(6).withMinute(50),
        origin = "昌平沙河高教园",
        note = "途径沙河-延庆，空两位。微信：ride4snow",
        isCanceled = false
    ),
    CarpoolItem(
        id = "cp3",
        resort = sampleResorts[2],
        departAt = LocalDateTime.now().minusDays(1).withHour(8).withMinute(0),
        origin = "长春站",
        note = "已取消，改期下周。",
        isCanceled = true
    )
)

private val sampleRoommateList = listOf(
    RoommateItem(
        id = "rm1",
        resort = sampleResorts[0],
        content = "12月10-12日两晚，求拼标间，女生优先，微信：snowcat",
        createdAt = LocalDateTime.now().minusHours(5),
        isCanceled = false
    ),
    RoommateItem(
        id = "rm2",
        resort = sampleResorts[3],
        content = "长白山万达12/20-12/23拼房，预算300/晚，男女不限。",
        createdAt = LocalDateTime.now().minusDays(2),
        isCanceled = false
    ),
    RoommateItem(
        id = "rm3",
        resort = sampleResorts[1],
        content = "圣诞节云顶三晚，已定Airbnb，还缺一位，微信：xmas-ski",
        createdAt = LocalDateTime.now().minusDays(1),
        isCanceled = true
    )
)
// endregion Data models & samples
