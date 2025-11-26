package com.gosnow.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TermsScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "用户协议与隐私政策") },
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
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TermsSection(title = "用户协议", content = userAgreementText)
            TermsSection(title = "隐私政策", content = privacyPolicyText)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val userAgreementText = """
欢迎使用 GoSnow！在注册与登录前，请仔细阅读并同意以下内容：

1. 账号使用：首次输入手机号会为你创建 GoSnow 账号，后续再次输入同一手机号即可直接登录。
2. 功能范围：登录后你可以浏览首页、雪圈与发现页，并在个人中心管理账号与退出登录。
3. 社区行为：请保持真实、友善的社区交流，不发布违法或侵权内容。
4. 账户安全：妥善保管验证码和设备安全，如发现异常可通过重新登录或联系我们支持团队。
""".trimIndent()

private val privacyPolicyText = """
GoSnow 致力于保护你的隐私：

1. 信息收集：我们仅在必要时收集手机号、验证码和基础设备信息，用于身份验证与安全风控。
2. 信息存储：登录态与用户 ID 会保存在本地存储并与 Supabase/Memfire 后端同步，用于维持会话。
3. 信息使用：你的数据仅用于提供核心滑雪社区功能，不会向未经授权的第三方出售或共享。
4. 权益与反馈：你可以通过退出登录清除本地会话，或联系我们申请更新、删除相关信息。
""".trimIndent()
