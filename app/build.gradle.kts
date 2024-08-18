val kotlinVersion: String by project

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 34
    namespace = "dev.seabat.android.usbdebugswitch"

    defaultConfig {
        applicationId = "dev.seabat.android.usbdebugswitch"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }

    buildTypes {
        getByName("debug") {
            setProperty("archivesBaseName", "${rootProject.name}-${defaultConfig.versionName}(${defaultConfig.versionCode})")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")

    // oss license
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

    // compose
    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-viewbinding:1.6.8")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
