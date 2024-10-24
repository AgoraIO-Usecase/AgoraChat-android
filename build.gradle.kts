// 新增 buildscript
buildscript {
    dependencies {
        // Note: If you use HuaWei or Honor push, you need to add the following dependencies
        classpath("com.android.tools.build:gradle:8.1.0")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.1" apply false
    // Add to replace of kapt plugin when using Room. 1.8.0 is the version of Kotlin, 1.0.9 is the version of KSP
    id("com.google.devtools.ksp") version "1.8.0-1.0.9" apply false
}

if(!project.hasProperty("isAarRelease")){
    project.ext.set("isAarRelease", false)
}
if(!project.hasProperty("isLite")){
    project.ext.set("isLite", false)
}
if(!project.hasProperty("sdkVersion")){
    project.ext.set("sdkVersion", "4.6.1")
}
if(!project.hasProperty("isTravis")) {
    project.ext.set("isTravis", false)
}