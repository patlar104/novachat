// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.0.0" apply false
    // Note: Kotlin Android plugin is now built into AGP 9.0.0
    // Compose compiler plugin for Kotlin 2.2.21 (CodeQL-compatible)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}
