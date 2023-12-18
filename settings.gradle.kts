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
    "java",
    "injection",
    "lib",
    "standalone"
)