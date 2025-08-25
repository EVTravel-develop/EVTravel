import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.compose)

    // Firebase Google Services
    id("com.google.gms.google-services")

    // Hilt
    id("com.google.dagger.hilt.android")
    // Kapt (Kotlin DSL에선 이 ID 그대로 사용 가능)
    id("kotlin-kapt")
}

val secretProperties = Properties().apply {
    val secretFile = rootProject.file("local.properties")
    if (secretFile.exists()) {
        load(secretFile.inputStream())
    } else {
        throw GradleException("local.properties file not found")
    }
}

val kakaoNativeKey = secretProperties.getProperty("KAKAO_NATIVE_APP_KEY")
    ?: throw GradleException("KAKAO_NATIVE_APP_KEY is missing in local.properties")

val kakaoRestKey = secretProperties.getProperty("KAKAO_REST_API_KEY")
    ?: throw GradleException("KAKAO_REST_API_KEY is missing in local.properties")

val evChargerKey = secretProperties.getProperty("EV_CHARGER_API_KEY") ?: ""

val kakaoNativeKeyDev  = secretProperties.getProperty("KAKAO_NATIVE_APP_KEY_DEV")  ?: kakaoNativeKey
val kakaoNativeKeyProd = secretProperties.getProperty("KAKAO_NATIVE_APP_KEY_PROD") ?: kakaoNativeKey
val kakaoRestKeyDev    = secretProperties.getProperty("KAKAO_REST_API_KEY_DEV")   ?: kakaoRestKey
val kakaoRestKeyProd   = secretProperties.getProperty("KAKAO_REST_API_KEY_PROD")  ?: kakaoRestKey
val evChargerKeyDev    = secretProperties.getProperty("EV_CHARGER_API_KEY_DEV")   ?: evChargerKey
val evChargerKeyProd   = secretProperties.getProperty("EV_CHARGER_API_KEY_PROD")  ?: evChargerKey
if (evChargerKeyDev.isBlank() && evChargerKeyProd.isBlank()) {
    throw GradleException("EV Charger API keys are missing. Set EV_CHARGER_API_KEY_DEV/PROD (or EV_CHARGER_API_KEY as a fallback).")
}

android {
    namespace = "com.jeju.evtravel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jeju.evtravel"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        // Kakao Map meta-data placeholder (Manifest에서 참조)
        manifestPlaceholders["KAKAO_MAP_KEY"] = kakaoNativeKey
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeKey
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            // 필요하면 debug 전용 설정 추가
        }
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "EVTravel Dev")
            // Manifest placeholder override (dev)
            manifestPlaceholders["KAKAO_MAP_KEY"] = kakaoNativeKeyDev
            manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeKeyDev

            // BuildConfig 로 노출 (dev)
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeKeyDev\"")
            buildConfigField("String", "KAKAO_REST_API_KEY", "\"$kakaoRestKeyDev\"")
            buildConfigField("String", "EV_CHARGER_API_KEY", "\"${evChargerKeyDev}\"")
        }
        create("prod") {
            dimension = "env"
            resValue("string", "app_name", "EVTravel")
            // Manifest placeholder override (prod)
            manifestPlaceholders["KAKAO_MAP_KEY"] = kakaoNativeKeyProd
            manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeKeyProd

            // BuildConfig 로 노출 (prod)
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeKeyProd\"")
            buildConfigField("String", "KAKAO_REST_API_KEY", "\"$kakaoRestKeyProd\"")
            buildConfigField("String", "EV_CHARGER_API_KEY", "\"${evChargerKeyProd}\"")
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
    // --- Android / Compose ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Material 2 (아이콘 확장은 아래)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material) // 필요 시 유지
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.foundation:foundation:1.6.1") // Pager 등
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // --- Permissions / Location ---
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // --- Kakao SDKs ---
    // 카카오 로그인
    implementation("com.kakao.sdk:v2-user:2.20.6")
    // 카카오 맵
    implementation("com.kakao.maps.open:android:2.12.8")

    // --- Networking (Retrofit + Moshi + OkHttp) ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // --- Hilt ---
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // --- Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")

    // --- 기타 ---
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.0.2")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Test ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
