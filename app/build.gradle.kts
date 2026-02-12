plugins {
    alias(libs.plugins.android.application)
    // Note: kotlin.android plugin is no longer needed in AGP 9.0.0 - Kotlin support is built-in
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.android.junit)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// Configure Kotlin compiler options for AGP 9.0+
kotlin {
    compilerOptions {
        // Set JVM target to Java 21
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

android {
    namespace = "com.novachat.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.novachat.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("main").kotlin.directories.add(
            "../generated/dataconnect/android/com/google/firebase/dataconnect/generated"
        )
    }
}

junitPlatform {
    instrumentationTests.behaviorForUnsupportedDevices =
    de.mannodermaus.gradle.plugins.junit5.dsl.UnsupportedDeviceBehavior.Skip
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM for version management
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    // Note: firebase-ai removed - using Firebase Functions proxy instead
    // Note: All KTX modules removed - KTX functionality now in main modules (BOM v34.0.0+)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.dataconnect)
    implementation(libs.firebase.functions)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // AICore for on-device AI
    // NOTE: AICore is experimental and not yet publicly available on Google Maven (as of Jan 2026)
    // For now, this dependency is commented out. On-device AI will be unavailable.
    // When AICore becomes available, uncomment this line:
    // implementation("androidx.ai.edge.aicore:aicore:1.0.0-alpha01")
    
    // Modules
    implementation(project(":feature-ai"))

    // Testing - Android Instrumented Tests
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(platform(libs.junit.bom))
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    
    // Testing - Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
