pluginManagement {
    repositories {
        jcenter()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
}

include(
    "firebase-common",
    "firebase-app",
    "firebase-firestore",
    "firebase-database",
    "firebase-auth",
    "firebase-functions"
)

//enableFeaturePreview("GRADLE_METADATA")

