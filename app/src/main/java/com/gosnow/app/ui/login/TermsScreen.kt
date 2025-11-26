package com.gosnow.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
                title = { Text(text = "服务条款与隐私") },
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            termsSections.forEach { section ->
                TermsCard(section = section)
            }
            Text(
                text = "感谢你阅读本《服务条款与隐私》，继续使用即表示你已理解并同意以上内容。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TermsCard(section: TermsSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            section.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class TermsSection(
    val title: String,
    val paragraphs: List<String>
)

private val termsSections = listOf(
    TermsSection(
        title = "一、适用范围与变更",
        paragraphs = listOf(
            "本条款适用于 GoSnow 提供的所有产品与服务，涵盖登录、账号管理及后续使用过程中的权利与义务。",
            "根据业务或法律变化，平台可能对条款进行更新，最新版本将以应用内展示为准，请及时留意。"
        )
    ),
    TermsSection(
        title = "二、账户与安全",
        paragraphs = listOf(
            "你需使用本人手机号进行登录并妥善保管验证码，不得将账号转让或出租给他人。",
            "如发现异常登录或安全风险，请及时修改登录方式或联系我们，避免损失。"
        )
    ),
    TermsSection(
        title = "三、注册与登录",
        paragraphs = listOf(
            "输入手机号并完成验证码校验后，将自动创建或登录你的 GoSnow 账号。",
            "请确保提供的手机号真实有效，验证码仅用于身份验证，不得用于其他目的。"
        )
    ),
    TermsSection(
        title = "四、用户信息与隐私",
        paragraphs = listOf(
            "平台会按必要原则收集登录所需的手机号、验证码等信息，用于身份核验与安全风控。",
            "你的信息将按照隐私政策进行存储和保护，未经授权不会向第三方披露，法律法规另有规定的除外。"
        )
    ),
    TermsSection(
        title = "五、授权与使用",
        paragraphs = listOf(
            "登录后即表示你授权平台在提供核心功能时使用必要的账号信息，以完成会话维持与服务推送。",
            "你应合理使用产品功能，不得通过技术手段干扰或破坏平台正常运行。"
        )
    ),
    TermsSection(
        title = "六、内容规范",
        paragraphs = listOf(
            "在社区或互动场景中发布的内容应遵守法律法规，尊重他人权益，不得包含违法、侵权、骚扰或其他不当信息。",
            "平台有权基于合规或安全原因对违规内容进行限制、屏蔽或处理。"
        )
    ),
    TermsSection(
        title = "七、服务变更与中止",
        paragraphs = listOf(
            "平台可能因系统维护、业务调整或不可抗力对部分功能进行变更、中止或终止，并尽可能提前告知。",
            "若因不可抗力导致服务中断，将在合理范围内协助用户恢复或提供补救措施。"
        )
    ),
    TermsSection(
        title = "八、免责声明",
        paragraphs = listOf(
            "在法律允许范围内，因网络、设备故障或第三方原因导致的服务不可用，平台不承担由此产生的间接损失。",
            "用户应自行确保设备与网络环境的安全，避免因个人原因引发的账号风险。"
        )
    ),
    TermsSection(
        title = "九、终止与注销",
        paragraphs = listOf(
            "如用户严重违反条款或相关法律法规，平台有权暂停或终止提供服务，并视情况限制账号使用。",
            "你可依据应用提供的路径退出登录或申请注销，注销后相关数据将按法律及政策要求处理。"
        )
    ),
    TermsSection(
        title = "十、未成年人保护",
        paragraphs = listOf(
            "若你为未成年人，应在监护人同意与指导下使用本产品，理性安排使用时间。",
            "监护人应加强对未成年人上网与信息安全的教育与监督。"
        )
    ),
    TermsSection(
        title = "十一、法律适用与争议解决",
        paragraphs = listOf(
            "本条款适用相关法律法规。如有争议，应先友好协商解决，协商不成的，提交有管辖权的法院处理。",
            "条款部分无效不影响其他条款的效力，平台可根据需要对无效条款进行调整。"
        )
    ),
    TermsSection(
        title = "十二、支持与联系",
        paragraphs = listOf(
            "如在使用过程中有疑问或需要支持，可通过应用内的反馈入口与我们联系。",
            "我们将尽力提供帮助，并在合理时间内回应你的问题或建议。"
        )
    )
)
