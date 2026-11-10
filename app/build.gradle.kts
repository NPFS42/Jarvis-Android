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
        versionCode = 3
        versionName = "0.1.2"
    }
    // Google Play builds never contain a private tailnet hostname.
    // Only the personal debug APK may use a local.properties bootstrap.
    signingConfigs {
        // Reuse NPFS's existing Kinetic Torch release/upload identity.
        // This environment interface is identical to the Kinetic Torch project.
        // No credentials, keystores or passwords are stored in this repository.
        val npfsKeyPath = System.getenv("KINETIC_KEYSTORE_PATH")
        if (!npfsKeyPath.isNullOrBlank()) {
            create("npfs") {
                storeFile = File(npfsKeyPath)
                storePassword = requireNotNull(System.getenv("KINETIC_KEYSTORE_PASSWORD")) {
                    "KINETIC_KEYSTORE_PASSWORD is required for NPFS release signing"
                }
                keyAlias = requireNotNull(System.getenv("KINETIC_KEY_ALIAS")) {
                    "KINETIC_KEY_ALIAS is required for NPFS release signing"
                }
                keyPassword = requireNotNull(System.getenv("KINETIC_KEY_PASSWORD")) {
                    "KINETIC_KEY_PASSWORD is required for NPFS release signing"
                }
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
            // Without the existing NPFS keystore, Gradle produces an unsigned
            // release. Never silently substitute a different signing identity.
            signingConfig = signingConfigs.findByName("npfs")
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
