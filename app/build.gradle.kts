plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.godongijo.ecotainment"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.godongijo.ecotainment"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Scalable sdp & ssp
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    // Multidex
    implementation(libs.androidx.multidex)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Image Slider
    implementation(libs.imageslideshow)

    // View Model Scope
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Glide
    implementation (libs.glide)

    // Circle Image
    implementation (libs.circleimageview)

    // Volley
    implementation(libs.volley)

    // Swipe Refresh Layout
    implementation(libs.androidx.swiperefreshlayout)

    // Facebook Shimmer
    implementation (libs.shimmer)

    // Retrofit
    implementation(libs.retrofit.v2110)
    implementation (libs.converter.gson.v2110)

    // Okhttp
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation (libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.constraintlayout.v220)
    implementation(libs.material.v1100)
    implementation(libs.material)


}