plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Add Firebase Plugin
}

android {
    namespace = "com.milk.milkrun"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.milk.milkrun"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("com.google.android.material:material:1.11.0' // or latest")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation ("com.google.android.material:material:1.10.0")
    implementation("com.airbnb.android:lottie:6.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

dependencies {
    implementation(libs.swiperefreshlayout)// Room Database
    implementation ("androidx.room:room-runtime:2.4.3")
    ("androidx.room:room-compiler:2.4.3")

    // Retrofit (API calls)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // WorkManager (Background tasks)
    implementation("androidx.work:work-runtime:2.7.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.material:material:1.11.0")


    // LiveData (Optional, for UI updates)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.6.2")
}
