plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.navigation.safeargs")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 37
    namespace = "dev.seabat.android.usbdebugswitch"

    defaultConfig {
        applicationId = "dev.seabat.android.usbdebugswitch"
        minSdk = 26
        targetSdk = 37
        versionCode = 11
        versionName = "1.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.legacy.preference.v14)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.constraintlayout)

    // coroutine
    implementation(libs.kotlinx.coroutines.core)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)

    // compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.viewbinding)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
