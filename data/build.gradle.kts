import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.pass.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val localProperties = Properties().apply {
            load(File("local.properties").inputStream())
        }

        val signalingServer: String? = localProperties["signalingServer"] as String?
        val turnServer: String? = localProperties["turnServer"] as String?
        val turnServerUserName: String? = localProperties["turnServerUserName"] as String?
        val turnServerUserPassword: String? = localProperties["turnServerUserPassword"] as String?

        if (signalingServer != null) {
            buildConfigField("String", "SignalingServer", signalingServer)
        }
        if (turnServer != null) {
            buildConfigField("String", "turnServer", turnServer)
        }
        if (turnServerUserName != null) {
            buildConfigField("String", "turnServerUserName", turnServerUserName)
        }
        if (turnServerUserPassword != null) {
            buildConfigField("String", "turnServerUserPassword", turnServerUserPassword)
        }
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kapt {
        correctErrorTypes = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            resources.excludes.add("META-INF/LICENSE.md")
            resources.excludes.add("META-INF/LICENSE-notice.md")
        }

        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {

    implementation(project(":domain"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // test
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-agent:1.13.8")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")

    // test with Uri(android)
    testImplementation("org.robolectric:robolectric:4.12.2")

    // webrtc
    implementation("io.getstream:stream-webrtc-android:1.1.3")

    // socket
    implementation ("io.socket:socket.io-client:2.0.1")

    // okhttp3
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}