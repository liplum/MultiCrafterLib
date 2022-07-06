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
    version = "1.0"
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
plugins {
    distribution
}
tasks {
    named<Zip>("distZip") {
        dependsOn(":lib:classes")
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