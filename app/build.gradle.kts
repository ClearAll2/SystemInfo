import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val buildCode = 39
val buildName = "3.4"
val appId = "com.lkonlesoft.displayinfo"

android {
    namespace = appId
    compileSdk = 36

    defaultConfig {
        applicationId = appId
        minSdk = 23
        targetSdk = 36
        versionCode = buildCode
        versionName = buildName
        resValue("string", "app_ver", "$buildName ($buildCode)")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += listOf("en", "vi", "nl", "fr", "de", "it", "pt", "es", "ru", "ja", "ko", "zh")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("11")
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation ("androidx.window:window:1.5.1")
    implementation ("androidx.glance:glance-appwidget:1.1.1")
    implementation ("androidx.glance:glance-material3:1.1.1")
    implementation("androidx.compose.material:material")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation ("androidx.appcompat:appcompat:1.7.1")
    implementation (platform("org.jetbrains.kotlin:kotlin-bom:2.2.20"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-ktx:1.12.1")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.9.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}