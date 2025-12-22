import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
}

android {
    namespace = "com.gosnow.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.gosnow.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 直接用 BuildConfig 暴露 Supabase 配置（跟你原来一样）
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"https://crals6q5g6h44cne3j40.baseapi.memfiredb.com\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImV4cCI6MzMwMjA1OTI5MSwiaWF0IjoxNzI1MjU5MjkxLCJpc3MiOiJzdXBhYmFzZSJ9.FYvFCQVIJn-iL-t9lxYOSzD__jJZMQMDtynLh-wTyHQ\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// 保留你原来的 localProperties 工具函数（以后想改成本地配置也方便）
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

fun getConfigOrEnv(key: String): String =
    (localProperties.getProperty(key) ?: System.getenv(key) ?: "")

val supabaseUrl: String = getConfigOrEnv("SUPABASE_URL")
val supabaseAnonKey: String = getConfigOrEnv("SUPABASE_ANON_KEY")
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    //implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.compose.material3)

    // 协程 & 网络（非 Supabase）
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ================= Supabase Kotlin（用 2.4.0 BOM）=================

    // 1. Supabase BOM：只给 BOM 写版本号
    implementation(platform("io.github.jan-tennert.supabase:bom:2.4.0"))

    // 2. 核心 SupabaseClient
    implementation("io.github.jan-tennert.supabase:supabase-kt")

    // 3. 认证（GoTrue）
    implementation("io.github.jan-tennert.supabase:gotrue-kt")

    // 4. 如果你要用数据库和存储，也一起先加上没问题
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")


    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.foundation.layout)
    // 以后需要再加：
    // implementation("io.github.jan-tennert.supabase:realtime-kt")
    // implementation("io.github.jan-tennert.supabase:functions-kt")

    // 5. Ktor（跟 2.4.0 兼容的版本）
    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // ================= 其它依赖 =================

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // 图片 & 地图
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.mapbox.maps:android:11.17.0")

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Material icons 扩展
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.play.services.auth)

    // Vico 图表
    val vicoVersion = "2.3.6"
    implementation("com.patrykandpatrick.vico:core:$vicoVersion")
    implementation("com.patrykandpatrick.vico:compose:$vicoVersion")
    implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")

    // JSON 序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ---------- 测试 ----------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

