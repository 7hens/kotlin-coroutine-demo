plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    val sdkVersion: Int by rootProject.extra
    compileSdkVersion(sdkVersion)
    signingConfigs {
        register("release") {
            val KEY_ALIAS: String by rootProject.extra
            val KEY_PASSWORD: String by rootProject.extra
            val STORE_FILE: String by rootProject.extra
            val STORE_PASSWORD: String by rootProject.extra
            keyAlias = KEY_ALIAS
            keyPassword = KEY_PASSWORD
            storeFile = rootProject.file(STORE_FILE)
            storePassword = STORE_PASSWORD
        }
    }
    defaultConfig {
        applicationId = "cn.thens.andemo"
        minSdkVersion(18)
        targetSdkVersion(sdkVersion)
        versionCode = 1
        versionName = "1.0"
        signingConfig = signingConfigs.getByName("release")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    lintOptions {
        setAbortOnError(false)
    }
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    testImplementation("junit:junit:4.12")
    testImplementation("org.mockito:mockito-core:2.22.0")
    testImplementation("org.mockito:mockito-inline:2.22.0")
    testImplementation("org.robolectric:robolectric:4.2.1")
    androidTestImplementation("org.mockito:mockito-android:2.22.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0-alpha02")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.recyclerview:recyclerview:1.0.0")

    implementation("io.reactivex.rxjava2:rxjava:2.2.10")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.uber.autodispose:autodispose-ktx:1.1.0")
    implementation("com.uber.autodispose:autodispose-android-archcomponents-ktx:1.1.0")

    implementation("com.squareup.retrofit2:retrofit:2.6.2")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.google.code.gson:gson:2.8.5")
}

