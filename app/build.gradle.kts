plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yourdomain.affy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourdomain.affy"
        minSdk = 34          // Pixel 9A ships Android 14 (API 34)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // TFLite model files must not be compressed — the Interpreter reads them via MappedByteBuffer
    androidResources {
        noCompress += listOf("tflite", "litertlm")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // LiteRT (TFLite) — local on-device inference for both Gemma hemispheres
    implementation("com.google.ai.edge.litert:litert:1.0.1")

    // WorkManager — nightly REM consolidation cycle
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Lifecycle coroutines — off-main-thread model loading in MainActivity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Core Android UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
