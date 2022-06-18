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
    "app",
    "lib",
)
include("app")
