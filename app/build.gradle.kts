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
    }

//이렇게 해서 오류가 계속 나던거였어 ; 계속 이렇게 했었는데, 버전이 다운그레이드 된건가,,,
    // AndroidStudio 4.0 ~
//    buildFeatures {
//        viewBinding = true
//        compose = true
//        dataBinding = true
//    }

//    // 이건 오류는 안나는데, 데이터 바인딩이 안됨
//    dataBinding{
//        enable = true
//    }

    // 이렇게 해서 데이터 바인딩. . .
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
    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-video:${camerax_version}")
    implementation ("androidx.camera:camera-view:${camerax_version}")
    implementation ("androidx.camera:camera-mlkit-vision:${camerax_version}")
    implementation ("androidx.camera:camera-extensions:${camerax_version}")
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

}