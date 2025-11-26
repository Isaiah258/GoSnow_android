package com.gosnow.app.ui.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val LandingBackground = Color(0xFF0A0A0A)
private val LandingCardBackground = Color(0xFF111111)
private val AccentBlue = Color(0xFF4A90E2)
private val MutedText = Color(0xFFB8B8B8)

@Composable
fun LoginLandingScreen(
    isCheckingSession: Boolean,
    onStartPhoneLogin: () -> Unit,
    onTermsClick: () -> Unit
) {
    if (isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LandingBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "正在检查登录状态…", color = Color.White)
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LandingBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "视频预留位",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = LandingCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "登录以继续",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "使用手机号登录，继续你的体验。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                    }

                    Button(
                        onClick = onStartPhoneLogin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text(text = "使用手机号登录", color = Color.White)
                    }

                    Text(
                        text = "《服务条款与隐私》",
                        color = AccentBlue,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { onTermsClick() },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneLoginScreen(
    uiState: LoginUiState,
    onPhoneChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    var countdownSeconds by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.codeSent) {
        if (uiState.codeSent) {
            countdownSeconds = 60
        }
    }

    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
        }
    }

    val termsText = remember { loginTermsAnnotatedString() }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = "手机号登录",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isCheckingSession) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "正在检查登录状态…")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "手机号登录",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "新用户点击登录将自动创建账号",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "手机号",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = uiState.phoneNumber,
                                onValueChange = onPhoneChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("输入 11 位手机号") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "验证码",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = uiState.verificationCode,
                                    onValueChange = onVerificationCodeChange,
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("输入 6 位验证码") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                val sendEnabled = !uiState.isSendingCode && countdownSeconds == 0
                                Button(
                                    onClick = onSendCode,
                                    enabled = sendEnabled,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) {
                                    when {
                                        uiState.isSendingCode -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        }
                                        countdownSeconds > 0 -> {
                                            Text(text = "重发(${countdownSeconds}s)", color = Color.White)
                                        }
                                        uiState.codeSent -> {
                                            Text(text = "重新发送", color = Color.White)
                                        }
                                        else -> {
                                            Text(text = "发送验证码", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!uiState.errorMessage.isNullOrEmpty()) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isVerifyingCode,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        if (uiState.isVerifyingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(text = "登录", color = Color.White)
                        }
                    }

                    ClickableText(
                        text = termsText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { offset ->
                            termsText.getStringAnnotations(tag = TERMS_TAG, start = offset, end = offset)
                                .firstOrNull()?.let { onTermsClick() }
                        }
                    )
                }
            }
        }
    }
}

private const val TERMS_TAG = "terms"

private fun loginTermsAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append("登录即表示同意")
        pushStringAnnotation(tag = TERMS_TAG, annotation = "terms")
        withStyle(SpanStyle(color = AccentBlue, fontWeight = FontWeight.SemiBold)) {
            append("《服务条款与隐私》")
        }
        pop()
    }
}
