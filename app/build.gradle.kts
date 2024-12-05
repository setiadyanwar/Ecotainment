plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
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
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

    // Okhttp
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation ("androidx.activity:activity-ktx:1.9.3")
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