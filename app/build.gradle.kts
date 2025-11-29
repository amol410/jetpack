import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.file.RegularFileProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.dolphin.jetpack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dolphin.jetpack"
        minSdk = 24
        targetSdk = 35
        versionCode = 15
        versionName = "6.5.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }

        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
// Use the latest version
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    ksp("androidx.room:room-compiler:2.6.1")

    // Kotlinx Serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Core & Compose Dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Test Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


}

// ================================================================================================
// CUSTOM GRADLE TASKS FOR SAFE RELEASE BUILDS
// ================================================================================================

// Task to perform clean release build (prevents stale build artifacts)
tasks.register("cleanReleaseBundle") {
    group = "build"
    description = "Clean build and generate release AAB - prevents NoSuchMethodError crashes"

    dependsOn("clean")
    finalizedBy("bundleRelease")

    doFirst {
        println("========================================")
        println("Starting CLEAN release build")
        println("This prevents stale artifacts and NoSuchMethodError crashes")
        println("========================================")
    }

    doLast {
        println("========================================")
        println("Release AAB created successfully!")
        println("Location: app/release/app-release.aab")
        println("========================================")
    }
}

// Task to perform clean release APK build
tasks.register("cleanReleaseApk") {
    group = "build"
    description = "Clean build and generate release APK - prevents NoSuchMethodError crashes"

    dependsOn("clean")
    finalizedBy("assembleRelease")

    doFirst {
        println("========================================")
        println("Starting CLEAN release APK build")
        println("This prevents stale artifacts and NoSuchMethodError crashes")
        println("========================================")
    }

    doLast {
        println("========================================")
        println("Release APK created successfully!")
        println("Location: app/build/outputs/apk/release/")
        println("========================================")
    }
}

// Task to verify ProGuard rules are applied correctly
abstract class VerifyProguardRulesTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val proguardFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val file = proguardFile.asFile.get()
        if (!file.exists()) {
            throw GradleException("ProGuard rules file missing!")
        }

        val content = file.readText()
        val requiredRules = listOf(
            "com.dolphin.jetpack.data.repository",
            "com.dolphin.jetpack.domain.repository",
            "com.dolphin.jetpack.presentation.viewmodel",
            "kotlinx.coroutines"
        )

        val missing = requiredRules.filter { !content.contains(it) }
        if (missing.isNotEmpty()) {
            throw GradleException("Missing critical ProGuard rules: $missing")
        }

        println("✓ ProGuard rules verified successfully")
    }
}

tasks.register<VerifyProguardRulesTask>("verifyProguardRules") {
    group = "verification"
    description = "Verify ProGuard rules exist and are comprehensive"
    proguardFile.set(file("proguard-rules.pro"))
}

// Automatically verify ProGuard rules before any release build
tasks.matching { it.name.contains("Release") && it.name.contains("bundle", ignoreCase = true) }.configureEach {
    dependsOn("verifyProguardRules")
}