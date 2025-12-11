import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


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

// 这里还是保留你原来的 localProperties 工具函数（虽然现在没用到）
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
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.compose.material3)

    // 协程 & 网络
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Supabase Kotlin (auth + Ktor client)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.4.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-client-logging:2.3.12")

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






    // JSON 序列化（你原来就有，保留）
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
