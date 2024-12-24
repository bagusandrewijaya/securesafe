plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.application.safesecure"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.application.safesecure"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Add publishing configuration
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.application"
            artifactId = "safesecure"
            version = "0.0.20"


        }
    }
}


dependencies {

    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.core:core:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}