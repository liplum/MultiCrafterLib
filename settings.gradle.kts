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
    "json-java",
    "injection",
    "lib",
    "standalone"
)