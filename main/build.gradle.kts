@file:Suppress("SpellCheckingInspection")

import io.github.liplum.mindustry.*

plugins {
    java
    id("io.github.liplum.mgpp") version "1.0.12"
}
sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
mindustry {
    dependency {
        mindustry mirror "d7312445a1"
        arc on "123fbf12b9"
    }
    client {
        mindustry be "22771"
    }
    server {
        mindustry be "22771"
    }
    mods {
        worksWith {
            add fromTask ":js:jar"
        }
    }
    deploy {
        baseName = "MultiCrafterLib"
    }
}
mindustryAssets {
    root at "$projectDir/assets"
}
dependencies {
    implementation(project(":lib"))
    importMindustry()
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
}
