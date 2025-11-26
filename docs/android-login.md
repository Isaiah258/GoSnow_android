# GoSnow Android 登录说明

## 登录入口
- `MainActivity` 入口会加载 `GoSnowApp`，根据是否存在本地会话决定展示登录页还是直接进入主界面。对应实现位于 `app/src/main/java/com/gosnow/app/ui/app/GoSnowApp.kt`。

## 主要类与文件
- **UI 层**：`LoginScreen`（`ui/login/LoginScreen.kt`）负责展示输入框、错误提示与按钮；`GoSnowMainApp`（`ui/app/GoSnowApp.kt`）承载 Home/Feed/Discover Tab，并在 Profile 页提供退出按钮。
- **状态管理**：`LoginViewModel`（`ui/login/LoginViewModel.kt`）负责表单校验、调用登录与登出，并监听本地会话变化。
- **数据层**：`AuthApiService` 调用 Supabase/Memfire GoTrue 密码登录接口；`AuthRepository` 组合网络与存储；`AuthPreferences` 基于 DataStore 持久化 access token / refresh token / userId。

## 登录状态存储与判断
- 登录成功后会将 `accessToken`、`refreshToken`（如果返回）、`userId` 与 `email` 写入 DataStore（`data/auth/AuthPreferences.kt`）。
- App 启动时 `LoginViewModel` 会订阅 DataStore，会话存在即视为已登录并跳转主界面；缺失则停留在登录页。
- 退出登录会清除上述 DataStore 条目，并重置页面到登录视图。

## 调试与配置
- Supabase/Memfire 配置通过 **`SUPABASE_URL`** 与 **`SUPABASE_ANON_KEY`** 注入，可在 `local.properties` 或环境变量中提供：
  ```properties
  SUPABASE_URL=https://your-project.supabase.co
  SUPABASE_ANON_KEY=your-anon-key
  ```
- 未配置时会直接提示错误，避免误调用。
- 登录流程使用 OkHttp 直接请求 `/auth/v1/token?grant_type=password`，确保后端配置一致即可在模拟器运行并调试。
