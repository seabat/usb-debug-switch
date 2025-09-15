
// Top-level build file where you can add configuration options common to all sub-projects/modules.

val kotlinVersion: String by extra("2.2.0")

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.4")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.8")
    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
}
