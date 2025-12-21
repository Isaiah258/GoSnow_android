pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()

        // ✅ 新增：Mapbox 下载仓库配置
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // 用户名固定为 mapbox
                username = "mapbox"
                // ✅ 密码填你刚刚提供的私钥
                password = "sk.eyJ1IjoiZ29zbm93IiwiYSI6ImNtaXdrY2o1NTBqMnQzZHF0c2lodDBhdmwifQ.Laqg31sJkGia-mc-LzK_aQ"
            }
        }
    }
}

rootProject.name = "gosnow"
include(":app")