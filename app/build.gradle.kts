plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.checkpill"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.checkpill"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 사용 중인 ABI 설정
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }

    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 중복 네이티브 파일 충돌 해결
    packagingOptions {
        pickFirst ("lib/arm64-v8a/libtensorflowlite_jni.so")
        pickFirst ("lib/x86_64/libtensorflowlite_jni.so")
        pickFirst ("lib/armeabi-v7a/libtensorflowlite_jni.so")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // CameraX
    val camerax_version = "1.4.0-beta02"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-mlkit-vision:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // PyTorch
    implementation("org.pytorch:pytorch_android_lite:1.10.0")
    implementation("org.pytorch:pytorch_android_torchvision_lite:1.10.0")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.15.0")
}
