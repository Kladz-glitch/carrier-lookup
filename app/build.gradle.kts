import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Chiave API e Client ID: di base si usano quelli predefiniti sotto; con local.properties puoi sostituirli.
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun secret(name: String, default: String = ""): String =
    localProps.getProperty(name, "").trim().ifEmpty { default }

// Valori predefiniti dell'app: se in local.properties non c'è una chiave propria si usano questi.
val defaultOmkarApiKey = "ok_269cbda9d7572c83ca949ce9cf0b4184"
val defaultGoogleWebClientId = "958659239181-85q7afma4tqvqq13jh5noe53hhmoolfv.apps.googleusercontent.com"

android {
    namespace = "com.example.carrierlookup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.carrierlookup"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "OMKAR_API_KEY", "\"${secret("omkar.apiKey", defaultOmkarApiKey)}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${secret("google.webClientId", defaultGoogleWebClientId)}\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
    implementation(libs.libphonenumber)
    implementation(libs.libphonenumber.carrier)
    implementation(libs.libphonenumber.geocoder)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
