@file:Suppress("SpellCheckingInspection")

plugins {
    java
}
val OutputJarName: String by project
version = "1.0"
group = "net.liplum"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register<Zip>("zip") {
    group = "build"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${OutputJarName}.zip")
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