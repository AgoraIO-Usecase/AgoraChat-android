import java.util.Properties
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    // Add the ksp plugin when using Room
    id("com.google.devtools.ksp")
}

val properties = Properties()
val inputStream = project.rootProject.file("local.properties").inputStream()
properties.load( inputStream )

android {
    namespace = "io.agora.chatdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.agora.chatdemo"
        minSdk = 21
        targetSdk = 34
        versionCode = 13
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Set app server info from local.properties
        buildConfigField ("String", "APP_SERVER_PROTOCOL", "\"https\"")
        buildConfigField ("String", "APP_SERVER_DOMAIN", "\"${properties.getProperty("APP_SERVER_DOMAIN")}\"")
        buildConfigField ("String", "APP_SERVER_URL", "\"${properties.getProperty("APP_SERVER_URL")}\"")
        buildConfigField ("String", "APP_SERVER_REGISTER", "\"${properties.getProperty("APP_SERVER_REGISTER")}\"")
        buildConfigField ("String", "APP_BASE_USER", "\"${properties.getProperty("APP_BASE_USER")}\"")
        buildConfigField ("String", "APP_UPLOAD_AVATAR", "\"${properties.getProperty("APP_UPLOAD_AVATAR")}\"")
        buildConfigField ("String", "APP_BASE_GROUP", "\"${properties.getProperty("APP_BASE_GROUP")}\"")
        buildConfigField ("String", "APP_GROUP_AVATAR", "\"${properties.getProperty("APP_GROUP_AVATAR")}\"")
        buildConfigField ("String", "APP_RTC_TOKEN_URL", "\"${properties.getProperty("APP_RTC_TOKEN_URL")}\"")
        buildConfigField ("String", "APP_RTC_CHANNEL_MAPPER_URL", "\"${properties.getProperty("APP_RTC_CHANNEL_MAPPER_URL")}\"")

        // Set appkey from local.properties
        buildConfigField("String", "AGORA_CHAT_APPKEY", "\"${properties.getProperty("AGORA_CHAT_APPKEY")}\"")
        // Set push info from local.properties
        buildConfigField("String", "FCM_SENDERID", "\"${properties.getProperty("FCM_SENDERID")}\"")
        // Set RTC appId from local.properties
        buildConfigField("String", "AGORA_APPID", "\"${properties.getProperty("AGORA_APPID")}\"")

        //指定room.schemaLocation生成的文件路径  处理Room 警告 Schema export Error
        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                ))
            }
        }

        ndk {
            abiFilters.addAll(mutableSetOf("arm64-v8a","armeabi-v7a"))
        }
        //用于设置使用as打包so时指定输出目录
        externalNativeBuild {
            ndkBuild {
                abiFilters("arm64-v8a","armeabi-v7a")
                arguments("-j8")
            }
        }


    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(properties.getProperty("DEBUG_STORE_FILE_PATH", "./keystore/sdkdemo.jks"))
            storePassword = properties.getProperty("DEBUG_STORE_PASSWORD", "123456")
            keyAlias = properties.getProperty("DEBUG_KEY_ALIAS", "easemob")
            keyPassword = properties.getProperty("DEBUG_KEY_PASSWORD", "123456")
        }
        create("release") {
            storeFile = file(properties.getProperty("RELEASE_STORE_FILE_PATH", "./keystore/sdkdemo.jks"))
            storePassword = properties.getProperty("RELEASE_STORE_PASSWORD", "123456")
            keyAlias = properties.getProperty("RELEASE_KEY_ALIAS", "easemob")
            keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD", "123456")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // Set toolchain version
    kotlin {
        jvmToolchain(8)
    }

    //打开注释后，可以直接在studio里查看和编辑emclient-linux里的代码
//    externalNativeBuild {
//        ndkBuild {
//            path = File("jni/Android.mk")
//        }
//    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    // lifecycle viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // Coil: load image library
    implementation("io.coil-kt:coil:2.5.0")
    // image corp library
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-material:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("pub.devrel:easypermissions:3.0.0")
    // Room
    implementation("androidx.room:room-runtime:2.5.1")
    ksp("androidx.room:room-compiler:2.5.1")
    // optional - Kotlin Extensions and Coroutines support for Room
    // To use Kotlin Flow and coroutines with Room, must include the room-ktx artifact in build.gradle file.
    implementation("androidx.room:room-ktx:2.5.1")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    // Declare the dependencies for the Firebase Cloud Messaging and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    implementation("io.agora.rtc:chat-uikit:1.3.0")
//    implementation(project(mapOf("path" to ":chat-uikit")))
    implementation("io.agora.rtc:chat-callkit:1.3.0")
//    implementation(project(mapOf("path" to ":chat-callkit")))
}