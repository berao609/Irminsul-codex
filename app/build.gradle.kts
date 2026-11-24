import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

val appVersionProperties = Properties().apply {
    val versionFile = rootProject.file("version.properties")
    require(versionFile.isFile) {
        "Missing version.properties at ${versionFile.absolutePath}"
    }
    versionFile.inputStream().use { load(it) }
}

val appVersionCode = appVersionProperties.getProperty("VERSION_CODE")
    ?.toIntOrNull()
    ?: error("VERSION_CODE must be a positive integer in version.properties")

val appVersionName = appVersionProperties.getProperty("VERSION_NAME")
    ?.takeIf { it.isNotBlank() }
    ?: error("VERSION_NAME must be set in version.properties")

require(appVersionCode > 0) { "VERSION_CODE must be greater than zero" }

val releaseKeystorePath = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
val releaseKeystorePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull

android {
    namespace = "com.irminsul.codex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.irminsul.codex"
        minSdk = 26
        targetSdk = 34
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
    }

    buildTypes {
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
}
