
// Top-level build file where you can add configuration options common to all sub-projects/modules.

val kotlinVersion: String by extra("2.2.0")

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
        classpath(libs.oss.licenses.plugin)
    }
}

plugins {
    id("com.android.application") version "9.3.2" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}
