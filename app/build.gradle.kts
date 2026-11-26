import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

val appVersionProperties = Properties().apply {
    val versionFile = rootProject.file("version.properties")
    require(versionFile.isFile) {
        "Missing version.properties at ${versionFile.absolutePath}"
    }
    versionFile.inputStream().use { load(it) }
}

// CI may override these values for uniquely numbered Debug builds. Official
// Release builds continue to use the values stored in version.properties.
val appVersionCode = providers.environmentVariable("APP_VERSION_CODE")
    .orNull
    ?.toIntOrNull()
    ?: appVersionProperties.getProperty("VERSION_CODE")
        ?.toIntOrNull()
        ?: error("VERSION_CODE must be a positive integer in version.properties")

val appVersionName = providers.environmentVariable("APP_VERSION_NAME")
    .orNull
    ?.takeIf { it.isNotBlank() }
    ?: appVersionProperties.getProperty("VERSION_NAME")
        ?.takeIf { it.isNotBlank() }
        ?: error("VERSION_NAME must be set in version.properties")

require(appVersionCode in 1..2_100_000_000) {
    "VERSION_CODE must be between 1 and 2,100,000,000"
}

val releaseKeystorePath = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
val releaseKeystorePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull

// CI supplies a dedicated persistent Debug keystore. Local development keeps
// Android's normal local Debug signing behavior when this path is absent.
val debugKeystorePath = providers.environmentVariable("DEBUG_KEYSTORE_PATH").orNull
val debugKeystorePassword = providers.environmentVariable("DEBUG_KEYSTORE_PASSWORD").orNull
val debugKeyAlias = providers.environmentVariable("DEBUG_KEY_ALIAS").orNull
val debugKeyPassword = providers.environmentVariable("DEBUG_KEY_PASSWORD").orNull

android {
    namespace = "com.irminsul.codex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.irminsul.codex"
        minSdk = 26
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        create("release") {
            // A missing keystore deliberately points to a nonexistent file.
            // This makes release packaging fail instead of creating an unsigned APK.
            storeFile = file(releaseKeystorePath ?: "missing-release-keystore.jks")
            storePassword = releaseKeystorePassword.orEmpty()
            keyAlias = releaseKeyAlias.orEmpty()
            keyPassword = releaseKeyPassword.orEmpty()
        }

        if (debugKeystorePath != null) {
            create("persistentDebug") {
                storeFile = file(debugKeystorePath)
                storePassword = debugKeystorePassword.orEmpty()
                keyAlias = debugKeyAlias.orEmpty()
                keyPassword = debugKeyPassword.orEmpty()
            }
        }
    }

    buildTypes {
        debug {
            // GitHub Actions uses one persistent certificate across all Debug
            // artifacts. Local builds retain the standard local debug key.
            if (debugKeystorePath != null) {
                signingConfig = signingConfigs.getByName("persistentDebug")
            }
        }

        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.lifecycleViewModelKtx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.coroutinesTest)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.room.testing)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
