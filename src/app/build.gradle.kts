plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val semanticVersion = rootProject.projectDir.parentFile.resolve("VERSION").readText().trim()
val versionParts = semanticVersion.split('.').map(String::toInt)
require(versionParts.size == 3) { "VERSION must be MAJOR.MINOR.PATCH" }

android {
    namespace = "com.keysersoze.screenlock"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.keysersoze.screenlock"
        minSdk = 26
        targetSdk = 37
        versionCode = versionParts[0] * 10_000 + versionParts[1] * 100 + versionParts[2]
        versionName = semanticVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
