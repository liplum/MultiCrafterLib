@file:Suppress("SpellCheckingInspection")

import io.github.liplum.mindustry.mindustry
import io.github.liplum.mindustry.mindustryAssets

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

version = "1.0"
group = "net.liplum"

mindustry {
    client {
        mindustry be "22771"
    }
    server {
        mindustry be "22771"
    }
    deploy {
        baseName = "TestInjection"
    }
}
tasks.jar {
    dependsOn(":lib:classes")
    from(project(":lib").buildDir.resolve("classes/java/main")){
        include("**/**")
    }
    from(project(":lib").projectDir.resolve("assets")) {
        include("**/**")
    }
}
mindustryAssets {
    root at "$projectDir/assets"
}
