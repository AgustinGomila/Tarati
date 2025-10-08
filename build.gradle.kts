buildscript {
    val androidGradlePluginVersion = libs.versions.agp.get()
    val kotlin = libs.versions.kotlin.get()

    dependencies {
        classpath("com.android.tools.build:gradle:$androidGradlePluginVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
    }

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}