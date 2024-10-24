pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.aliyun.com/repository/public/")}
        maven { url = uri("https://jitpack.io")}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/public/")}
        maven { url = uri("https://jitpack.io")}
    }
}

rootProject.name = "AgoraChat-android"
//include(":app")
//include(":chat-uikit")
//project(":chat-uikit").projectDir = File("../AgoraChat-UIKit-android/chat-uikit")

// app-kotlin
include(":app-kotlin")
include(":chat-uikit")
project(":chat-uikit").projectDir = File("/Users/wt/Downloads/work/apex_sdk_work/base/chatuikit-android/ease-im-kit")

include(":chat-callkit")
project(":chat-callkit").projectDir = File("../AgoraChat-CallKit-android/chat-callkit")

include(":hyphenatechatsdk")
project(":hyphenatechatsdk").projectDir = File("../emclient-android/hyphenatechatsdk")

include(":ease-linux")
project(":ease-linux").projectDir = File("../emclient-linux")
