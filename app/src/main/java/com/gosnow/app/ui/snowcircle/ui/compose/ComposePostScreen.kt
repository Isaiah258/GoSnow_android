package com.gosnow.app.ui.snowcircle.ui.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gosnow.app.ui.snowcircle.data.PostRepository
import com.gosnow.app.ui.snowcircle.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val CONTENT_LIMIT = 500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(navController: NavController, viewModel: ComposePostViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val canPublish = uiState.content.isNotBlank() && uiState.resortName.isNotBlank() && !uiState.isPosting

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        viewModel.onImagesSelected(uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("取消")
                    }
                },
                title = { Text("发布") },
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
                        colors = TextButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
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
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "选择雪场", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = uiState.resortName,
                onValueChange = viewModel::onResortChange,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = CircleShape,
                leadingIcon = { Icon(imageVector = Icons.Outlined.Place, contentDescription = null) },
                placeholder = { Text("搜索雪场以发布") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "正文", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("这里输入正文") },
                minLines = 4,
                maxLines = 8
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "${uiState.content.length}/$CONTENT_LIMIT",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "图片", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ImageSelector(
                images = uiState.selectedImages,
                onAddClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
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
private fun ImageSelector(images: List<Uri>, onAddClick: () -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images) { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }
        if (images.size < 4) {
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddClick)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Outlined.Image, contentDescription = null)
                        Text("添加图片", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

data class ComposePostUiState(
    val resortName: String = "",
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

    fun onResortChange(value: String) {
        _uiState.update { it.copy(resortName = value.trimStart().take(50)) }
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
                    resortName = state.resortName,
                    images = state.selectedImages.map { it.toString() },
                    currentUser = currentUser
                )
            }.onSuccess {
                _uiState.value = ComposePostUiState()
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isPosting = false, errorMessage = e.message ?: "发布失败") }
            }
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
