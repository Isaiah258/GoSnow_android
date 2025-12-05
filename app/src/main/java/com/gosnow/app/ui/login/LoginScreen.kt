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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gosnow.app.R
import androidx.compose.material3.TextFieldDefaults


// ====== 公共样式 & 常量 ======

private val AccentBlue = Color(0xFF4A90E2)
private const val TERMS_TAG = "terms"

private const val APP_NAME = "上雪"
private const val SUPPORT_EMAIL = "gosnow.serviceteam@gmail.com"
private const val TERMS_LAST_UPDATED = "2025-10-17"

// iOS 同款逻辑：手机号 11 位，验证码 6 位
private fun canSendOtp(phone: String): Boolean =
    phone.filter { it.isDigit() }.length == 11

private fun canVerify(phone: String, code: String): Boolean =
    canSendOtp(phone) && code.filter { it.isDigit() }.length == 6

// ====== 欢迎页：顶部视频预留 + 底部黑色卡片 ======
//WelcomeAuthIntroScreen//
@Composable
fun WelcomeAuthIntroScreen(
    isCheckingSession: Boolean,
    onStartPhoneLogin: () -> Unit,
    onTermsClick: () -> Unit
) {
    if (isCheckingSession) {
        // 保持原来的 loading 样式
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
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
            .background(Color.Black)
    ) {
        // 顶部：视频贴在最上方，宽度铺满，高度你可以继续用自适应版本
        IntroVideoPlayer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // 底部：黑色卡片贴着底部，不再占满整个剩余高度
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = (-80).dp),   // ★ 往上抬 32dp，减少中间黑块
            color = Color.Black,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp), // ★ 原来是 24.dp，缩小顶部空白
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "登录以继续",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "使用手机号登录，继续你的体验。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onStartPhoneLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "使用手机号登录",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


            }
        }

    }

}

@Composable
fun IntroVideoPlayer(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val maxHeight = (configuration.screenHeightDp / 2f).dp   // 最多占屏幕一半高度

    var videoAspect by remember { mutableStateOf<Float?>(null) }

    // 按是否拿到宽高比来自适应 + 限制最大高度
    val sizedModifier = modifier
        .then(
            if (videoAspect != null) {
                Modifier.aspectRatio(videoAspect!!)
            } else {
                Modifier.height(240.dp)   // 首帧占位高度
            }
        )
        .heightIn(max = maxHeight)        // 关键：限制最大高度

    AndroidView(
        modifier = sizedModifier,
        factory = { ctx ->
            VideoView(ctx).apply {
                val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.login_intro_top}")
                setVideoURI(uri)

                setOnPreparedListener { mp ->
                    val w = mp.videoWidth
                    val h = mp.videoHeight
                    if (w > 0 && h > 0) {
                        // 自动按原始宽高比自适应
                        videoAspect = w.toFloat() / h.toFloat()
                    }
                    mp.isLooping = true
                    mp.setVolume(0f, 0f)
                    start()
                }

                setOnCompletionListener {
                    start()
                }
            }
        },
        update = { videoView ->
            if (!videoView.isPlaying) {
                videoView.start()
            }
        }
    )
}



// ====== 登录页：手机号 + 验证码 ======

@OptIn(ExperimentalMaterial3Api::class)
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

    // 发送成功后启动 60s 倒计时（对应 iOS resendSeconds）
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

    val phoneDigits = uiState.phoneNumber.filter { it.isDigit() }
    val codeDigits = uiState.verificationCode.filter { it.isDigit() }

    val canSend = canSendOtp(phoneDigits) && !uiState.isSendingCode && countdownSeconds == 0
    val canLogin = canVerify(phoneDigits, codeDigits) && !uiState.isVerifyingCode

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            if (uiState.isCheckingSession) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "正在检查登录状态…",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // 大黑卡片 + 滚动表单，仿 iOS LoginView
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp
                            )
                        ),
                    color = Color.Black
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 标题
                        Text(
                            text = "手机号登录",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "新用户点击登录将自动创建账号",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // 手机号
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "手机号",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            androidx.compose.material3.OutlinedTextField(
                                value = uiState.phoneNumber,
                                onValueChange = { raw ->
                                    val cleaned = raw.filter { it.isDigit() }.take(11)
                                    onPhoneChange(cleaned)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("输入 11 位手机号") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone
                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF111111),
                                    unfocusedContainerColor = Color(0xFF111111),
                                    disabledContainerColor = Color(0xFF111111),
                                    cursorColor = Color.White,
                                    focusedIndicatorColor = Color.White.copy(alpha = 0.8f),
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                                )
                            )



                        }

                        // 验证码 + 发送按钮
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "验证码",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = uiState.verificationCode,
                                    onValueChange = { raw ->
                                        val cleaned = raw.filter { it.isDigit() }.take(6)
                                        onVerificationCodeChange(cleaned)
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("输入 6 位验证码") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF111111),
                                        unfocusedContainerColor = Color(0xFF111111),
                                        disabledContainerColor = Color(0xFF111111),
                                        cursorColor = Color.White,
                                        focusedIndicatorColor = Color.White.copy(alpha = 0.8f),
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )



                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = onSendCode,
                                    enabled = canSend,
                                    modifier = Modifier.widthIn(min = 110.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,                               // 可用：白底
                                        contentColor = Color.Black,                                 // 可用：黑字
                                        disabledContainerColor = Color.White.copy(alpha = 0.18f),   // 禁用：淡一点
                                        disabledContentColor = Color.Black.copy(alpha = 0.45f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    when {
                                        uiState.isSendingCode -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.Black
                                            )
                                        }
                                        countdownSeconds > 0 -> {
                                            Text(text = "重发(${countdownSeconds}s)")
                                        }
                                        uiState.codeSent -> {
                                            Text(text = "重新发送")
                                        }
                                        else -> {
                                            Text(text = "发送验证码")
                                        }
                                    }
                                }

                            }
                        }

                        // 错误提示
                        if (!uiState.errorMessage.isNullOrEmpty()) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // 登录按钮（白底黑字，仿 iOS）
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canLogin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,                         // 可用：纯白底
                                contentColor = Color.Black,                           // 可用：黑字
                                disabledContainerColor = Color.White.copy(alpha = 0.14f), // 禁用：略淡的白
                                disabledContentColor = Color.Black.copy(alpha = 0.35f)    // 禁用：略淡的黑
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (uiState.isVerifyingCode) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.Black
                                )
                            } else {
                                Text(
                                    text = "登录",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }


                        // 协议文案
                        ClickableText(
                            text = termsText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 8.dp),
                        ) { offset ->
                            termsText
                                .getStringAnnotations(
                                    tag = TERMS_TAG,
                                    start = offset,
                                    end = offset
                                )
                                .firstOrNull()
                                ?.let { onTermsClick() }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private fun loginTermsAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append("登录即表示你同意")
        pushStringAnnotation(tag = TERMS_TAG, annotation = "terms")
        withStyle(
            SpanStyle(
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append("《服务条款与隐私》")
        }
        pop()
    }
}

// ====== 服务条款页 ======

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "服务条款与隐私",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题与更新时间
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$APP_NAME 服务条款与隐私摘要",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "最后更新：$TERMS_LAST_UPDATED",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "请在使用本应用前仔细阅读。本页面为要点摘要，完整条款与隐私政策以我们在应用内或官方网站公布的正式版本为准。继续使用即表示你同意这些条款及其后续修订。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TermsSection(
                title = "一、适用范围与变更",
                bullets = listOf(
                    "本条款约束你对 $APP_NAME 应用及相关服务（“服务”）的访问与使用。",
                    "我们可能不时更新条款与隐私政策；更新后在应用内生效并替代旧版本。",
                    "我们可在不通知的情况下改进或变更产品与功能，或临时/永久地变更、暂停或终止服务的全部或部分。"
                )
            )

            TermsSection(
                title = "二、账户与安全",
                bullets = listOf(
                    "你负责妥善保管账户与密码，并对账户下的所有活动负责。",
                    "我们采取合理安全措施，但无法保证绝对安全。若发现未授权使用或安全问题，请立即联系我们。",
                    "13 岁以下儿童不应使用本应用。"
                )
            )

            TermsSection(
                title = "三、可接受的使用（AUP）",
                bullets = listOf(
                    "不得上传或传播违法、侵权、辱骂、仇恨、低俗或其他令人反感的内容。",
                    "不得冒充他人、散布垃圾信息/广告、上传恶意代码、破坏/干扰服务或网络。",
                    "不得以自动化方式（脚本、爬虫、机器人等）未经授权访问服务。",
                    "不得违反适用的法律法规及第三方平台规则。",
                    "如违反 AUP，$APP_NAME 可采取包括限制/终止账户在内的措施。"
                )
            )

            TermsSection(
                title = "四、用户内容与授权",
                bullets = listOf(
                    "你对自己上传/发布的内容承担全部责任。",
                    "你授予 $APP_NAME 全球范围、不可撤销、免版税的许可，以为提供与推广服务之目的而使用、复制、修改、展示、分发该等内容（在法律允许范围内）。",
                    "地图、徽标及第三方受版权保护的信息归其权利人所有；应权利人要求，我们可删除相关内容。",
                    "应用内的度假村/场地信息仅供参考，你应自行核验。"
                )
            )

            TermsSection(
                title = "五、软件与知识产权",
                bullets = listOf(
                    "服务及其中的软件与内容受知识产权保护。除法律允许外，不得对其进行复制、修改、反向工程或衍生利用。",
                    "$APP_NAME 授予你个人、不可转让、非独占的访问与使用许可；不得通过非官方接口访问服务。",
                    "我们保留本协议未明确授予的全部权利。"
                )
            )

            TermsSection(
                title = "六、隐私摘要",
                bullets = listOf(
                    "我们依据隐私政策收集与处理必要信息（如账户信息、设备与使用数据）。",
                    "在符合法律的前提下，我们可能为合规、保障安全、履行合同、客服支持等目的访问、保存或披露必要信息。",
                    "完整内容请查看《隐私政策》正式文本。"
                )
            )

            TermsSection(
                title = "七、免责声明与责任限制",
                bullets = listOf(
                    "服务按“现状/可用”提供，我们不对其满足你的特定需求、不间断、无错误或结果完全准确作出保证。",
                    "在法律允许范围内，我们不对因使用或无法使用服务而导致的任何直接或间接损失承担责任。"
                )
            )

            TermsSection(
                title = "八、医疗免责声明（如适用）",
                bullets = listOf(
                    "$APP_NAME 不提供医疗建议，应用内内容不能替代专业医疗指导。若有健康问题请咨询专业医生；紧急情况请拨打当地急救电话。"
                )
            )

            TermsSection(
                title = "九、终止",
                bullets = listOf(
                    "在以下情形下，我们可在不事先通知的情况下立即终止或限制你对服务的访问：违反本条款、执法/监管要求、长时间不活跃、技术/安全问题、欠费等。",
                    "终止后可能删除与账户相关的信息并限制再次使用服务。"
                )
            )

            TermsSection(
                title = "十、沟通与电子通知",
                bullets = listOf(
                    "你同意我们以电子方式向你提供通知、披露与其他通信，并视为满足书面形式要求。",
                    "应用内的论坛/群聊等属公开交流环境，请谨慎发布内容。"
                )
            )

            TermsSection(
                title = "十一、第三方设备与材料",
                bullets = listOf(
                    "使用服务可能需要第三方设备或材料。我们不对第三方设备/材料的兼容性、可用性或无错误性作保证。"
                )
            )

            TermsSection(
                title = "十二、支持与联系",
                bullets = listOf(
                    "我们通过电子邮件提供支持：$SUPPORT_EMAIL"
                )
            )

            Text(
                text = "如你不同意上述条款，请停止使用本应用。继续使用即表示你同意本条款及其后续修订。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    bullets: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            bullets.forEach { text ->
                TermsBullet(text)
            }
        }
    }
}

@Composable
private fun TermsBullet(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}




