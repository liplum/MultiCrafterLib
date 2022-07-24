import io.github.liplum.mindustry.version

plugins {
    distribution
    `maven-publish`
    id("io.github.liplum.mgpp") version "1.1.7"
}
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}
allprojects {
    group = "net.liplum"
    version = "1.1"
    buildscript {
        repositories {
            maven { url = uri("https://www.jitpack.io") }
        }
    }
    repositories {
        mavenCentral()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            excludeTags("slow")
        }
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}
mindustry {
    dependency {
        mindustry mirror "v136"
        arc on "v136"
    }
    client {
        mindustry official "v136.1"
    }
    server {
        mindustry official "v136.1"
    }
}
tasks {
    named<Zip>("distZip") {
        dependsOn(":lib:classes")
        archiveVersion.set(mindustry.meta.version)
        from(project("lib").buildDir.resolve("classes/java/main")) {
            include("**/**")
        }
        from(project("lib").projectDir.resolve("assets")) {
            include("**/**")
        }
    }
    register("getReleaseHeader") {
        doLast {
            println("::set-output name=header::${rootProject.name} v$version on Mindustry v136")
            println("::set-output name=version::v$version")
        }
    }
}