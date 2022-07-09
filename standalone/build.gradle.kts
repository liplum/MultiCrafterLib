@file:Suppress("SpellCheckingInspection")

import io.github.liplum.mindustry.mindustry
import io.github.liplum.mindustry.mindustryAssets

plugins {
    java
    id("io.github.liplum.mgpp")
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

mindustry {
    deploy {
        baseName = "TestInjection"
    }
}

mindustryAssets {
    root at "$projectDir/assets"
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