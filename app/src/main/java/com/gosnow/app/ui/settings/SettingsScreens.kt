package com.gosnow.app.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gosnow.app.ui.theme.GosnowTheme
import kotlinx.coroutines.launch
// 顶部 import 里补充这些：
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage


import com.gosnow.app.datasupabase.ProfileRepository
import com.gosnow.app.datasupabase.CurrentUserStore
import com.gosnow.app.util.loadAndCompressImage
import kotlinx.coroutines.launch


const val ROUTE_SETTINGS = "settings"
const val ROUTE_ACCOUNT_PRIVACY = "settings_account_privacy"
const val ROUTE_FEEDBACK = "settings_feedback"
const val ROUTE_ABOUT = "settings_about"
const val ROUTE_EDIT_PROFILE = "settings_edit_profile"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String,
    avatarUrl: String?,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAccountPrivacyClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "账户与设置",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 顶部用户信息
            UserInfoCard(
                userName = userName,
                avatarUrl = avatarUrl,
                onEditProfileClick = onEditProfileClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = "通用",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 通用设置分组
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SettingsItemRow(
                    icon = Icons.Filled.ManageAccounts,
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = "账户与隐私",
                    onClick = onAccountPrivacyClick
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingsItemRow(
                    icon = Icons.Filled.Feedback,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = "用户反馈",
                    onClick = onFeedbackClick
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingsItemRow(
                    icon = Icons.Filled.Info,
                    iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    title = "关于我们",
                    onClick = onAboutClick
                )
            }

            // 退出登录分组
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SettingsItemRow(
                    icon = Icons.Filled.Logout,
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.onErrorContainer,
                    title = "退出登录",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = onLogoutClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }


}

@Composable
private fun UserInfoCard(
    userName: String,
    avatarUrl: String?,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
// 简单的首字母头像占位
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "G"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }


            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "已登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(onClick = onEditProfileClick) {
                Text(text = "编辑资料")
            }
        }
    }


}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint)
        }


        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }


}

/* ------------ 子页面：账户与隐私 ------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPrivacyScreen(
    onBackClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onOpenSystemSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "账户与隐私",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "管理你的账户安全、隐私与权限，确保信息安全并按需控制应用授权。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SettingsItemRow(
                    icon = Icons.Filled.DeleteForever,
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.onErrorContainer,
                    title = "注销账户",
                    titleColor = MaterialTheme.colorScheme.error,
                    subtitle = "注销后数据将按照法律要求处理",
                    onClick = onDeleteAccountClick
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingsItemRow(
                    icon = Icons.Filled.Lock,
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = "权限管理",
                    subtitle = "前往系统设置管理应用权限",
                    onClick = onOpenSystemSettingsClick
                )
            }

            Text(
                text = "提示：Android 可使用 Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) 跳转到应用详情页，在外层实现具体逻辑。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }


}

/* ------------ 子页面：反馈 ------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "用户反馈",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "感谢你的反馈！请尽可能详细地描述问题或建议，方便我们快速优化体验。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("反馈标题（可选）") }
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请描述你遇到的问题或建议…") },
                        minLines = 5
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("联系方式（可选）") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null
                            )
                        }
                    )
                    Button(
                        onClick = {
                            onSubmitClick(title, content)
                            scope.launch {
                                snackbarHostState.showSnackbar("反馈已提交，我们会尽快处理！")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "提交反馈")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }


}

/* ------------ 子页面：关于 ------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onCommunityGuidelinesClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "关于我们",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GoSnow",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "版本 v1.0.0",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "一款陪你记录雪场足迹、分享滑雪故事的社区应用。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SettingsItemRow(
                    icon = Icons.Filled.Star,
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = "社区准则",
                    onClick = onCommunityGuidelinesClick
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingsItemRow(
                    icon = Icons.Filled.Info,
                    iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = "隐私政策",
                    subtitle = "可复用登录流程中的条款页面逻辑",
                    onClick = onPrivacyPolicyClick
                )
            }

            Text(
                text = "隐私政策可复用登录流程中的 Terms/Privacy 页面，这里只负责导航。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }


}

/* ------------ 子页面：编辑资料 ------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    avatarUrl: String?,
    onBackClick: () -> Unit,
    // 如果你后面希望把新昵称/头像回传给上层，可以把 onSaveClick 换成 onProfileUpdated: (String, String?) -> Unit
    onSaveClick: (String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentName) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 新增：选中的头像 Uri（本地）
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 保存中状态
    var isSaving by remember { mutableStateOf(false) }


    // 相册选择器（只选择图片）
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    // 是否有改动：昵称 or 头像
    val hasChanges by remember(name, selectedImageUri) {
        mutableStateOf(
            name.trim() != currentName.trim() || selectedImageUri != null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "编辑资料",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 头像区域：优先显示本地 Uri，其次远程 avatarUrl，最后首字母占位
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "新头像预览",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            !avatarUrl.isNullOrBlank() -> {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "当前头像",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "G"
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isSaving
                    ) {
                        Text(text = "更换头像")
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("昵称") },
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("昵称不能为空")
                                }
                                return@Button
                            }

                            scope.launch {
                                try {
                                    isSaving = true

                                    // 压缩头像（如果有选新的）
                                    val avatarBytes = selectedImageUri?.let { uri ->
                                        loadAndCompressImage(context, uri)
                                    }

                                    // 调用 ProfileRepository 更新 Supabase
                                    val newAvatarUrl = ProfileRepository.updateProfile(
                                        nickname = trimmed,
                                        avatarBytes = avatarBytes,
                                        currentAvatarUrl = avatarUrl
                                    )

                                    // ✅ 本地也更新一份，避免重新打网络
                                    CurrentUserStore.updateLocalProfile(
                                        newName = trimmed,
                                        newAvatarUrl = newAvatarUrl
                                    )

                                    // 如果你外层还想拿到新昵称，可以保留原来的回调
                                    onSaveClick(trimmed)

                                    snackbarHostState.showSnackbar("已保存")
                                    onBackClick()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    snackbarHostState.showSnackbar(
                                        "保存失败：${e.message ?: "请稍后重试"}"
                                    )
                                } finally {
                                    isSaving = false
                                }
                            }

                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = hasChanges && !isSaving
                    ) {
                        Text(text = if (isSaving) "保存中…" else "保存")
                    }
                }
            }
        }
    }
}


/* ------------ 预览 ------------ */

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    GosnowTheme {
        SettingsScreen(
            userName = "ifyoulikewinter",
            avatarUrl = null,
            onBackClick = {},
            onEditProfileClick = {},
            onAccountPrivacyClick = {},
            onFeedbackClick = {},
            onAboutClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountPrivacyScreenPreview() {
    GosnowTheme {
        AccountPrivacyScreen(
            onBackClick = {},
            onDeleteAccountClick = {},
            onOpenSystemSettingsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedbackScreenPreview() {
    GosnowTheme {
        FeedbackScreen(
            onBackClick = {},
            onSubmitClick = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    GosnowTheme {
        AboutScreen(
            onBackClick = {},
            onCommunityGuidelinesClick = {},
            onPrivacyPolicyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    GosnowTheme {
        EditProfileScreen(
            currentName = "滑雪爱好者",
            avatarUrl = null,
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
