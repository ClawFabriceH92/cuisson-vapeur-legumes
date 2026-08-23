// Android application module — Compose UI, Room, Hilt, AlarmManager/notifications.
//
// Built by CI (.github/workflows/build-apk.yml) on a GitHub-hosted runner —
// this sandbox has no Android SDK and its proxy blocks dl.google.com, so it
// cannot compile this module itself (see root README, "Limitations connues").
//
// Since 23/08/2026: release builds are signed with a dedicated, stable
// keystore (secrets CUISSON_KEYSTORE_B64 / CUISSON_KEYSTORE_PASSWORD /
// CUISSON_KEY_PASSWORD / CUISSON_KEY_ALIAS on GitHub) so that in-app
// auto-update can install a newer APK over an older one. Debug builds stay
// unsigned (their per-run debug.keystore would break signature continuity).
import java.io.File
import java.util.Base64

plugins {
    id("com.android.application") version "8.5.2"
    id("org.jetbrains.kotlin.android") version "2.0.21"
    // Required since Kotlin 2.0: the Compose compiler is no longer bundled
    // into AGP's composeOptions{} mechanism, it's its own Gradle plugin.
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("com.google.dagger.hilt.android") version "2.51.1"
}

// Reads the release keystore either from the CI secrets (CUISSON_KEYSTORE_B64
// in env) or from the local backup file ($HOME/.secrets/cuisson-release.keystore).
fun releaseKeystore(): File? {
    System.getenv("CUISSON_KEYSTORE_B64")?.let { b64 ->
        val tmp = File(System.getenv("RUNNER_TEMP") ?: "/tmp", "cuisson-release.keystore")
        tmp.writeBytes(Base64.getDecoder().decode(b64))
        return tmp
    }
    val local = File(System.getProperty("user.home"), ".secrets/cuisson-release.keystore")
    return if (local.exists()) local else null
}

android {
    namespace = "com.trucdecomptable.cuissonvapeur"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.trucdecomptable.cuissonvapeur"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val ks = releaseKeystore()
            if (ks != null) {
                storeFile = ks
                storePassword = System.getenv("CUISSON_KEYSTORE_PASSWORD")
                    ?: "CHANGE_ME"
                keyAlias = System.getenv("CUISSON_KEY_ALIAS")
                    ?: "cuisson"
                keyPassword = System.getenv("CUISSON_KEY_PASSWORD")
                    ?: System.getenv("CUISSON_KEYSTORE_PASSWORD")
                    ?: "CHANGE_ME"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        // Needed by UpdateChecker (BuildConfig.VERSION_NAME) — AGP 8 disables it by default.
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":domain"))

    // --- Compose ---
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Classic Material Components XML library — needed only for the
    // pre-Compose window theme (Theme.Material3.DayNight.NoActionBar in
    // themes.xml, used for the splash screen / status bar before setContent{}
    // hands off to Compose). The Compose Material3 artifact above has no XML
    // styles of its own.
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // --- Room ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // --- Hilt ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // Deliberately NOT depending on androidx.hilt:hilt-work (nor its
    // androidx.hilt:hilt-compiler): this app schedules everything through
    // AlarmManager (see alarm/AlarmScheduler.kt) and uses no Worker at all.
    // hilt-work drags in an old androidx.work, whose androidx.startup
    // auto-initializer ran ForceStopRunnable on every launch and crashed the
    // process on Android 12+ ("Targeting S+ requires FLAG_IMMUTABLE or
    // FLAG_MUTABLE"). Do not re-add it without a real WorkManager use case
    // and a current work-runtime version.

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- DataStore (settings) ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- Tests ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.json:json:20231013") // UpdateChecker parse tests (org.json mocked otherwise)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
