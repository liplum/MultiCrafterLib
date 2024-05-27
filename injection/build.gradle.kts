@file:Suppress("SpellCheckingInspection")

import io.github.liplum.mindustry.importMindustry
import io.github.liplum.mindustry.mindustryAssets

plugins {
    java
    id("io.github.liplum.mgpp")
}
sourceSets {
    main {
        java.srcDirs("src")
    }
    test {
        java.srcDir("test")
    }
}
mindustryAssets {
    root at "$projectDir/assets"
}
tasks.jar{
    from(projectDir.resolve("assets")){
        include("scripts/lib.js")
    }
}
dependencies {
    implementation(project(":lib"))
    importMindustry()
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
}
