import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services")
}

// KAKAO_NATIVE_APP_KEY 관리
val secretProperties = Properties().apply {
    val secretFile = rootProject.file("local.properties")
    if (secretFile.exists()) load(secretFile.inputStream())
    else throw GradleException("local.properties file not found")
}
val kakaoKey = secretProperties.getProperty("KAKAO_NATIVE_APP_KEY")
    ?: throw GradleException("KAKAO_NATIVE_APP_KEY is missing in local.properties")

android {
    namespace = "com.jeju.evtravel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jeju.evtravel"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${kakaoKey}\"")
        manifestPlaceholders["KAKAO_MAP_KEY"] = kakaoKey
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI & Material3 (Compose UI만 쓸 경우 material 만)
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("com.kakao.sdk:v2-user:2.19.0")
    // Dialogs (Sheets)
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.0.2")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2")

    // Kakao Map SDK
    implementation("com.kakao.maps.open:android:2.12.8")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // Google Play Services - Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
