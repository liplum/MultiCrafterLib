@file:Suppress("SpellCheckingInspection")

plugins {
    java // For zip
}
version = "1.0"
group = "net.liplum"

tasks.register<Zip>("zip") {
    group = "build"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("MultiCrafterTest.zip")
    includeEmptyDirs = false
    from("$projectDir/src") {
        include("**/")
    }
    from(projectDir) {
        // add something into your Jar
        include("mod.hjson")
        include("icon.png")
    }

    from("$projectDir/assets") {
        include("**")
    }
}