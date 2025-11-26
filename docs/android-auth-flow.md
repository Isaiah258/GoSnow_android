# Android 认证流程说明

本说明对应 Kotlin + Jetpack Compose 的登录/注册体验，尽量与 iOS 端对齐。

## 导航入口
- **WelcomeAuthIntroScreen**：未登录启动 App 时的起始页面（`welcome_auth` 路由）。主按钮文案为“使用手机号以继续”。
- **PhoneLoginScreen**：手机号 + 验证码登录/注册页面（`phone_login` 路由）。
- **TermsScreen**：用户协议与隐私政策内容页（`terms` 路由）。从欢迎页或登录页的文案点击进入。
- 登录成功后跳转到主框架 `GoSnowMainApp`（首页/雪圈/发现 Tab）。

## 状态与持久化
- `LoginViewModel` 暴露 `LoginUiState`，包含手机号、验证码、发送/验证状态、错误信息、登录态与 `isCheckingSession` 标记。
- `AuthRepository` 负责调用 `AuthApiService` 与 `AuthPreferences`，将 Supabase 返回的 `AuthSession` 写入 DataStore 持久化（access token、refresh token、userId、email）。
- 启动时 `AuthPreferences.session` 会被收集以判断是否已登录，决定导航起点。

## 主要交互
- WelcomeAuthIntroScreen：主按钮进入手机号登录；卡片文案可点击查看条款。
- PhoneLoginScreen：
  - 输入手机号 → 发送验证码（首次视为注册，后续视为登录）。
  - 输入验证码 → 触发 `verifyCodeAndLogin()` 登录并持久化会话。
  - 底部文案的“用户协议/隐私政策”可点击，进入 TermsScreen。
- TermsScreen：滚动展示“用户协议”和“隐私政策”两段内容，顶部提供返回按钮。
- 退出登录：`GoSnowMainApp` 接收 `onLogout` 回调，调用 `LoginViewModel.logout()` 清除 DataStore，并将导航回 `welcome_auth`。

## Supabase/Memfire 配置
- 构建时从 `local.properties` 或环境变量读取 `SUPABASE_URL` 与 `SUPABASE_ANON_KEY`，并注入到 `BuildConfig`。
- 相关代码：`app/build.gradle.kts` 中的 `buildConfigField`，`AuthApiService.ensureSupabaseConfigured()` 负责缺失配置的报错提示。

## 本地调试步骤
1. 在项目根目录创建 `local.properties`（或导出环境变量）：
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```
2. 运行 `./gradlew assembleDebug` 或直接在 Android Studio 运行应用。
3. App 启动后依次体验：欢迎页 → "使用手机号以继续" → 输入手机号并发送验证码 → 输入验证码 → 登录成功跳转主界面。
4. 在欢迎页或登录页点击“用户协议/隐私政策”可以查看 TermsScreen。
