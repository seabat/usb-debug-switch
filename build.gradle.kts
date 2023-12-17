// Top-level build file where you can add configuration options common to all sub-projects/modules.

val kotlinVersion: String by extra("1.9.0")

plugins {
    id("com.android.application") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}