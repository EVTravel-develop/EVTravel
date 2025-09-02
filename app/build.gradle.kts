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
        secretFile.inputStream().use { load(it) }
    }
}

fun getSecret(key: String): String =
    (providers.gradleProperty(key).orNull
        ?: providers.environmentVariable(key).orNull
        ?: secretProperties.getProperty(key)
        ?: "").trim()


val kakaoNativeKey = getSecret("KAKAO_NATIVE_APP_KEY")
val kakaoRestKey   = getSecret("KAKAO_REST_API_KEY")
val evChargerKey = getSecret("EV_CHARGER_API_KEY")

val kakaoNativeKeyDev  = getSecret("KAKAO_NATIVE_APP_KEY_DEV").ifBlank { kakaoNativeKey }
val kakaoNativeKeyProd = getSecret("KAKAO_NATIVE_APP_KEY_PROD").ifBlank { kakaoNativeKey }
val kakaoRestKeyDev    = getSecret("KAKAO_REST_API_KEY_DEV").ifBlank { kakaoRestKey }
val kakaoRestKeyProd   = getSecret("KAKAO_REST_API_KEY_PROD").ifBlank { kakaoRestKey }
val evChargerKeyDev    = getSecret("EV_CHARGER_API_KEY_DEV").ifBlank { evChargerKey }
val evChargerKeyProd   = getSecret("EV_CHARGER_API_KEY_PROD").ifBlank { evChargerKey }

val missing = mutableListOf<String>()
// 플래버별 키 유효성 검사
fun effective(vararg candidates: String) = candidates.firstOrNull { it.isNotBlank() }.orEmpty()

// Dev 플래버에 주입될 키
val devNative  = effective(kakaoNativeKeyDev, kakaoNativeKey)
val devRest    = effective(kakaoRestKeyDev,   kakaoRestKey)
val devCharger = effective(evChargerKeyDev,   evChargerKey)

// Prod 플래버에 주입될 키
val prodNative  = effective(kakaoNativeKeyProd, kakaoNativeKey)
val prodRest    = effective(kakaoRestKeyProd,   kakaoRestKey)
val prodCharger = effective(evChargerKeyProd,   evChargerKey)

// 어떤 플래버를 빌드 중인지 간단 추론 (IDE Sync/구성 단계에선 비강제)
val tasks = gradle.startParameter.taskNames.map { it.lowercase() }
fun anyTaskMatches(vararg regexes: Regex) =
    tasks.any { t -> regexes.any { r -> r.containsMatchIn(t) } }
// assemble/bundle/install/connectedAndroidTest 등 일반적인 작업명의 Dev/Prod 변형만 허용
val buildingDev = anyTaskMatches(
    Regex("""(?<![a-z])assembledev(debug|release)"""),
    Regex("""(?<![a-z])bundledev(debug|release)"""),
    Regex("""(?<![a-z])installdev(debug|release)"""),
    Regex("""connecteddevdebugandroidtest""")
)
val buildingProd = anyTaskMatches(
    Regex("""(?<![a-z])assembleprod(debug|release)"""),
    Regex("""(?<![a-z])bundleprod(debug|release)"""),
    Regex("""(?<![a-z])installprod(debug|release)"""),
    Regex("""connectedproddebugandroidtest""")
)

if (buildingDev) {
    if (devNative.isBlank())  missing += "dev: KAKAO_NATIVE_APP_KEY"
    if (devRest.isBlank())    missing += "dev: KAKAO_REST_API_KEY"
    if (devCharger.isBlank()) missing += "dev: EV_CHARGER_API_KEY"
}
if (buildingProd) {
    if (prodNative.isBlank())  missing += "prod: KAKAO_NATIVE_APP_KEY"
    if (prodRest.isBlank())    missing += "prod: KAKAO_REST_API_KEY"
    if (prodCharger.isBlank()) missing += "prod: EV_CHARGER_API_KEY"
}

if (missing.isNotEmpty()) {
    throw GradleException("Missing secrets for requested flavors: ${missing.joinToString()}")
}
if (!buildingDev && !buildingProd) {
    logger.lifecycle("Note: No specific flavor task detected; skipping strict secret validation.")
}

android {
    namespace = "com.jeju.evtravel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jeju.evtravel"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        // Kakao Map meta-data placeholder (Manifest에서 참조)
        manifestPlaceholders["KAKAO_MAP_KEY"] = kakaoNativeKey
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeKey
        // IDE/툴링 호환을 위한 기본값
        manifestPlaceholders["USES_CLEARTEXT"] = false
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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
            manifestPlaceholders["USES_CLEARTEXT"] = true
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "EVTravel Dev")
            // Manifest placeholder override (dev)
            manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = devNative
            manifestPlaceholders["KAKAO_MAP_KEY"]        = devNative

            // BuildConfig 로 노출 (dev)
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$devNative\"")
            buildConfigField("String", "KAKAO_REST_API_KEY", "\"$devRest\"")
            buildConfigField("String", "EV_CHARGER_API_KEY", "\"$devCharger\"")
        }
        create("prod") {
            manifestPlaceholders["USES_CLEARTEXT"] = false
            dimension = "env"
            // Manifest placeholder override (prod)
            manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = prodNative
            manifestPlaceholders["KAKAO_MAP_KEY"]        = prodNative

            // BuildConfig 로 노출 (prod)
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$prodNative\"")
            buildConfigField("String", "KAKAO_REST_API_KEY", "\"$prodRest\"")
            buildConfigField("String", "EV_CHARGER_API_KEY", "\"$prodCharger\"")
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
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
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

    // Crashlytics SDK
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

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
