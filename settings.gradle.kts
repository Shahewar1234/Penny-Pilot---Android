// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral() // ESSENTIAL
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
   // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ✅ Required for MPAndroidChart
    }
}

rootProject.name = "Penny Pilot"
include(":app")
