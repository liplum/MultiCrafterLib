rootProject.name = "MultiCrafter"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
include(
    "main",
    "js",
    "lib",
    "standalone"
)