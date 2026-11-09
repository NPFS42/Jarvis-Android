import java.util.Properties
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// The home bootstrap hostname is LOCAL to the builder. It is not committed.
val localConfig = Properties().also { props ->
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { props.load(it) }
}
val bootstrap = (localConfig.getProperty("jarvis.defaultHost") ?: "").trim().lowercase()
require(bootstrap.isEmpty() || Regex("^[a-z0-9-]+\\.[a-z0-9-]+\\.ts\\.net$").matches(bootstrap)) {
    "jarvis.defaultHost must be an exact MagicDNS name (node.tailnet.ts.net)"
}
android {
    namespace = "net.sovereignresearch.jarvis"
    compileSdk = 36
    defaultConfig {
        applicationId = "net.sovereignresearch.jarvis"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    // Google Play builds never contain a private tailnet hostname.
    // Only the personal debug APK may use a local.properties bootstrap.
    signingConfigs {
        if (System.getenv("JARVIS_UPLOAD_KEYSTORE") != null &&
            System.getenv("JARVIS_UPLOAD_PASSWORD") != null) {
            create("upload") {
                storeFile = File(System.getenv("JARVIS_UPLOAD_KEYSTORE"))
                storePassword = System.getenv("JARVIS_UPLOAD_PASSWORD")
                keyAlias = "jarvis-upload"
                keyPassword = System.getenv("JARVIS_UPLOAD_PASSWORD")
                storeType = "PKCS12"
            }
        }
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "DEFAULT_HOST", "\"$bootstrap\"")
        }
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            buildConfigField("String", "DEFAULT_HOST", "\"\"")
            if (signingConfigs.findByName("upload") != null)
                signingConfig = signingConfigs.getByName("upload")
        }
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isReturnDefaultValues = true }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
