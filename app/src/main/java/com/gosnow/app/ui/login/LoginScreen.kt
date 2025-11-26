package com.gosnow.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.gosnow.app.BuildConfig
import com.gosnow.app.R

private val iOSBlue = Color(0xFF0A84FF)
private val iOSBackground = Color(0xFFF2F3F5)
private val iOSMuted = Color(0xFF6E6E73)

@Composable
fun WelcomeAuthIntroScreen(
    isCheckingSession: Boolean,
    onStartPhoneLogin: () -> Unit,
    onTermsClick: () -> Unit
) {
    var hasInit by remember { mutableStateOf(false) }

    LaunchedEffect(isCheckingSession) {
        if (!isCheckingSession) {
            hasInit = true
        }
    }

    if (isCheckingSession && !hasInit) {
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
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(iOSBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFE9F2FF), Color(0xFFDCE8FA))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E5E8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "GoSnow",
                            modifier = Modifier.size(48.dp),
                            colorFilter = ColorFilter.tint(iOSBlue)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "欢迎来到 GoSnow",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "与 iOS 版本一致的认证入口，保持品牌色与体验节奏。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = iOSMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "使用手机号以继续",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111114)
                )
                Text(
                    text = "与 iOS 登录入口保持同样的结构和说明，让首次注册和再次登录都一目了然。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = iOSMuted
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IntroChecklistItem(text = "手机号登录/注册一体化")
                    IntroChecklistItem(text = "验证码登录确保账号安全")
                    IntroChecklistItem(text = "Memfire Supabase 同步会话")
                }
            }

            Button(
                onClick = onStartPhoneLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
            ) {
                Text(text = "使用手机号以继续", color = Color.White)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "登录即代表你同意我们的《用户协议》和《隐私政策》",
                        style = MaterialTheme.typography.bodyMedium,
                        color = iOSMuted
                    )
                    Text(
                        text = "查看条款",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = iOSBlue,
                        modifier = Modifier.clickable { onTermsClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroChecklistItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = iOSBlue
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1C1C1E))
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
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(iOSBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = iOSBlue
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "返回",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = iOSBlue
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "手机号验证",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111114)
                )
                Text(
                    text = "与你的 iOS 登录页保持文案对齐，验证码登录和注册二合一",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSMuted
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("手机号") },
                        placeholder = { Text(text = "请输入手机号") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Button(
                        onClick = onSendCode,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSendingCode,
                        colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
                    ) {
                        if (uiState.isSendingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(text = if (uiState.codeSent) "重新发送验证码" else "发送验证码", color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = uiState.verificationCode,
                        onValueChange = onVerificationCodeChange,
                        label = { Text("验证码") },
                        placeholder = { Text(text = "请输入短信验证码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            if (!uiState.errorMessage.isNullOrEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isVerifyingCode,
                colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
            ) {
                if (uiState.isVerifyingCode) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(text = "验证码登录", color = Color.White)
                }
            }

            Divider(color = Color(0xFFE5E5EA))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Memfire Supabase 配置",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "URL：${BuildConfig.SUPABASE_URL}",
                    style = MaterialTheme.typography.bodySmall,
                    color = iOSMuted
                )
                Text(
                    text = "Anon Key：${BuildConfig.SUPABASE_ANON_KEY}",
                    style = MaterialTheme.typography.bodySmall,
                    color = iOSMuted
                )
                Text(
                    text = "与 iOS 版本一致，明确展示 Memfire（Supabase）端点信息。",
                    style = MaterialTheme.typography.bodySmall,
                    color = iOSMuted
                )
            }

            val termsText = remember { termsAnnotatedString() }
            ClickableText(
                text = termsText,
                style = MaterialTheme.typography.bodySmall.copy(color = iOSMuted),
                modifier = Modifier.fillMaxWidth(),
                onClick = { offset ->
                    termsText.getStringAnnotations(tag = TERMS_TAG, start = offset, end = offset)
                        .firstOrNull()?.let { onTermsClick() }
                }
            )
        }
    }
}

private const val TERMS_TAG = "terms"

private fun termsAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append("登录即代表你同意")
        append(" “")
        pushStringAnnotation(tag = TERMS_TAG, annotation = "terms")
        withStyle(SpanStyle(color = iOSBlue, fontWeight = FontWeight.SemiBold)) {
            append("用户协议")
        }
        pop()
        append("” 和 “")
        pushStringAnnotation(tag = TERMS_TAG, annotation = "terms")
        withStyle(SpanStyle(color = iOSBlue, fontWeight = FontWeight.SemiBold)) {
            append("隐私政策")
        }
        pop()
        append("”。")
    }
}
