import com.android.build.gradle.internal.dsl.NdkOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Función para leer la versión desde el archivo properties
fun getVersionProperties(): Properties {
    val versionFile = file("version.properties")
    val versionProps = Properties()
    versionProps.load(versionFile.reader())
    return versionProps
}

val versionProps = getVersionProperties()

android {
    namespace = "com.agustin.tarati"
    compileSdk = libs.versions.compileSdk.get().toInt()
    ndkVersion = libs.versions.ndk.get()

    defaultConfig {
        applicationId = "com.agustin.tarati"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = versionProps.getProperty("versionCode").toInt()
        versionName = versionProps.getProperty("versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk.debugSymbolLevel = NdkOptions.DebugSymbolLevel.SYMBOL_TABLE.toString()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmCompatibility.get())
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // DataStore
    implementation(libs.datastore)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // ViewModels
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Tareas para gestionar versiones
tasks.register("incrementVersionCode") {
    doLast {
        val versionFile = file("version.properties")
        val versionProps = Properties()
        versionProps.load(versionFile.reader())

        val currentCode = versionProps.getProperty("versionCode").toInt()
        versionProps.setProperty("versionCode", (currentCode + 1).toString())
        versionProps.store(versionFile.writer(), null)

        println("✅ VersionCode incrementado a: ${currentCode + 1}")
    }
}

tasks.register("setVersionName") {
    doLast {
        val versionName = project.findProperty("newVersionName") as? String
            ?: throw GradleException("Debes proporcionar newVersionName: ./gradlew setVersionName -PnewVersionName=1.1.0")

        val versionFile = file("version.properties")
        val versionProps = Properties()
        versionProps.load(versionFile.reader())

        versionProps.setProperty("versionName", versionName)
        versionProps.store(versionFile.writer(), null)

        println("✅ VersionName actualizado a: $versionName")
    }
}

tasks.register("showVersion") {
    doLast {
        val versionProps = getVersionProperties()
        println("📱 Versión actual: ${versionProps.getProperty("versionName")} (${versionProps.getProperty("versionCode")})")
    }
}