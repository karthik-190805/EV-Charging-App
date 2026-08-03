plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.evapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.evapp"
        minSdk = 29
        targetSdk = 36
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.preference:preference:1.2.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.code.gson:gson:2.11.0")

    // ✅ ADD THIS - OSMDroid BonusPack for clustering
    implementation("com.github.MKergall:osmbonuspack:6.9.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
