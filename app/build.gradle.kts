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


        buildConfigField("String", "SUPABASE_URL", "\"https://crals6q5g6h44cne3j40.baseapi.memfiredb.com\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImV4cCI6MzMwMjA1OTI5MSwiaWF0IjoxNzI1MjU5MjkxLCJpc3MiOiJzdXBhYmFzZSJ9.FYvFCQVIJn-iL-t9lxYOSzD__jJZMQMDtynLh-wTyHQ\"")




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
    }

    buildFeatures {
        buildConfig = true
    }
}

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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(libs.okhttp)
    implementation(libs.androidx.datastore.preferences)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Jetpack Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Material icons 扩展
    implementation("androidx.compose.material:material-icons-extended")
}