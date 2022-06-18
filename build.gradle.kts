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
tasks {
    register("test") {
        group = "verification"
        doLast {
            allprojects.forEach {
                it.tasks.withType<Test>().forEach { test ->
                    test.executeTests()
                }
            }
        }
    }
}