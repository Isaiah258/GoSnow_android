package com.gosnow.app.ui.snowcircle.ui.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gosnow.app.datasupabase.SupabaseClientProvider
import com.gosnow.app.ui.snowcircle.data.PostRepository
import com.gosnow.app.ui.snowcircle.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val CONTENT_LIMIT = 500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(navController: NavController, viewModel: ComposePostViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // 必须有正文 + 选择雪场，才能发布
    val canPublish = uiState.content.isNotBlank() &&
            uiState.resortName.isNotBlank() &&
            !uiState.isPosting

    val focusManager = LocalFocusManager.current

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        viewModel.onImagesSelected(uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("取消")
                    }
                },
                actions = {
                    TextButton(
                        enabled = canPublish,
                        onClick = {
                            scope.launch {
                                viewModel.publish {
                                    navController.popBackStack()
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (canPublish)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(if (uiState.isPosting) "发布中" else "发布")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        viewModel.hideResortSuggestions()
                    })
                }
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 雪场选择区域（输入框 + 下拉候选）
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "选择雪场", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = uiState.resortName,
                onValueChange = viewModel::onResortChange,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                placeholder = { Text("搜索雪场以发布") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // ✅ 候选列表：只在输入且有结果时显示
            if (uiState.showResortSuggestions && (uiState.isSearchingResort || uiState.resortSuggestions.isNotEmpty())) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    if (uiState.isSearchingResort) {
                        Text(
                            text = "搜索中…",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp)
                        ) {
                            items(uiState.resortSuggestions.size) { idx ->
                                val item = uiState.resortSuggestions[idx]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onResortSuggestionPicked(item.nameResort)
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.nameResort,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 正文
            Text(text = "正文", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("这里输入正文") },
                minLines = 4,
                maxLines = 8,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${uiState.content.length}/$CONTENT_LIMIT",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图片
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "图片", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ImageSelector(
                images = uiState.selectedImages,
                onAddClick = {
                    picker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemove = viewModel::removeImage
            )


            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageSelector(
    images: List<Uri>,
    onAddClick: () -> Unit,
    onRemove: (Uri) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images) { uri ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 右上角删除按钮
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable { onRemove(uri) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (images.size < 4) {
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onAddClick)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text("添加图片", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}


@Serializable
data class ResortRow(
    val id: Long? = null,
    @SerialName("name_resort")
    val nameResort: String
)

data class ComposePostUiState(
    val resortName: String = "",

    // ✅ 新增：候选列表状态（不影响发布逻辑）
    val resortSuggestions: List<ResortRow> = emptyList(),
    val showResortSuggestions: Boolean = false,
    val isSearchingResort: Boolean = false,

    val content: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val isPosting: Boolean = false,
    val errorMessage: String? = null
)

class ComposePostViewModel(
    private val postRepository: PostRepository,
    private val currentUser: User
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposePostUiState())
    val uiState: StateFlow<ComposePostUiState> = _uiState.asStateFlow()

    private var resortSearchJob: Job? = null

    fun onResortChange(value: String) {
        val trimmed = value.trimStart().take(50)
        _uiState.update {
            it.copy(
                resortName = trimmed,
                // 输入就显示候选
                showResortSuggestions = trimmed.isNotBlank(),
                // 先清空上一轮结果，避免“无反应”的错觉
                resortSuggestions = if (trimmed.isBlank()) emptyList() else it.resortSuggestions
            )
        }

        // 空就直接收起
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(resortSuggestions = emptyList(), isSearchingResort = false, showResortSuggestions = false) }
            resortSearchJob?.cancel()
            return
        }

        // ✅ debounce + 后端搜索
        resortSearchJob?.cancel()
        resortSearchJob = viewModelScope.launch {
            delay(250)
            searchResorts(trimmed)
        }
    }

    fun hideResortSuggestions() {
        _uiState.update { it.copy(showResortSuggestions = false) }
    }

    fun onResortSuggestionPicked(name: String) {
        _uiState.update {
            it.copy(
                resortName = name,
                showResortSuggestions = false,
                resortSuggestions = emptyList(),
                isSearchingResort = false
            )
        }
    }
    fun removeImage(uri: Uri) {
        _uiState.update { state ->
            state.copy(selectedImages = state.selectedImages.filterNot { it == uri })
        }
    }

    private suspend fun searchResorts(keyword: String) {
        _uiState.update { it.copy(isSearchingResort = true, showResortSuggestions = true) }

        runCatching {
            val supabase = SupabaseClientProvider.supabaseClient

            // ✅ 关键：字段是 name_resort，不是 name
            val rows: List<ResortRow> = supabase
                .from("Resorts_data")
                .select(
                    columns = Columns.list("id", "name_resort")
                ) {
                    filter {
                        ilike("name_resort", "%$keyword%")
                    }
                    order("name_resort", Order.ASCENDING)
                    limit(20)
                }
                .decodeList()

            rows
        }.onSuccess { list ->
            // 防止异步回来的结果覆盖了用户最新输入
            if (_uiState.value.resortName != keyword && !_uiState.value.resortName.contains(keyword, ignoreCase = true)) {
                _uiState.update { it.copy(isSearchingResort = false) }
                return
            }
            _uiState.update {
                it.copy(
                    isSearchingResort = false,
                    resortSuggestions = list,
                    showResortSuggestions = true,
                    errorMessage = null
                )
            }
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    isSearchingResort = false,
                    resortSuggestions = emptyList(),
                    // 这里别用 errorMessage（那是发布失败提示），避免把“搜雪场失败”当成发布失败
                    showResortSuggestions = true
                )
            }
        }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value.take(CONTENT_LIMIT)) }
    }

    fun onImagesSelected(uris: List<Uri>) {
        _uiState.update { state ->
            val merged = (state.selectedImages + uris).distinct().take(4)
            state.copy(selectedImages = merged)
        }
    }

    fun publish(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.content.isBlank() || state.resortName.isBlank()) return@launch

            _uiState.update { it.copy(isPosting = true, errorMessage = null) }

            runCatching {
                postRepository.createPost(
                    content = state.content,
                    resortName = state.resortName, // 仍然只传 name
                    images = state.selectedImages.map { it.toString() },
                    currentUser = currentUser
                )
            }.onSuccess {
                _uiState.value = ComposePostUiState()
                onSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isPosting = false,
                        errorMessage = e.message ?: "发布失败"
                    )
                }
            }
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
