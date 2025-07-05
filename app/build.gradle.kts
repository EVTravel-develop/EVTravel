import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"

    //Firebase Google Services Plugin
    id("com.google.gms.google-services")
}
val secretProperties = Properties().apply {
    val secretFile = rootProject.file("local.properties")
    if (secretFile.exists()) {
        load(secretFile.inputStream())
    } else {
        throw GradleException(".secret.properties file not found")
    }
}

val kakaoKey = secretProperties.getProperty("KAKAO_NATIVE_APP_KEY")
    ?: throw GradleException("KAKAO_NATIVE_APP_KEY is missing in .secret.properties")


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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // 위치 서비스
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")

    // Kakao Map SDK
    implementation("com.kakao.maps.open:android:2.12.8")

    //Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    //Firebase Analytics 예시
    implementation ("com.google.firebase:firebase-database-ktx:20.0.4")

    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.0.2")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}