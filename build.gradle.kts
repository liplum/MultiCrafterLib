plugins {
    `maven-publish`
    id("io.github.liplum.mgpp") version "1.1.10"
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
    version = "1.7"
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
        useJUnitPlatform()
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}
mindustry {
    dependency {
        mindustry mirror "v145"
        arc on "v145"
    }
    client {
        mindustry official "v145"
    }
    server {
        mindustry official "v145"
    }
    run {
        clearOtherMods
    }
}

tasks.register<net.liplum.DistributeInjection>("distInjection") {
    group = "build"
    dependsOn(":injection:deploy")
    jar.from(tasks.getByPath(":injection:deploy"))
    name.set("MultiCrafterLib-injection.zip")
    excludeFiles.add(File("icon.png"))
    excludeFiles.add(File("mod.hjson"))
    excludeFolders.add(File("META-INF"))
}

tasks.register("retrieveMeta") {
    doLast {
        println("::set-output name=header::${rootProject.name} v$version on Mindustry v136")
        println("::set-output name=version::v$version")
        try {
            val releases = java.net.URL("https://api.github.com/repos/liplum/MultiCrafterLib/releases").readText()
            val gson = com.google.gson.Gson()
            val info = gson.fromJson<List<Map<String, Any>>>(releases, List::class.java)
            val tagExisted = info.any {
                it["tag_name"] == "v$version"
            }
            println("::set-output name=tag_exist::$tagExisted")
        } catch (e: Exception) {
            println("::set-output name=tag_exist::false")
            logger.warn("Can't fetch the releases", e)
        }
    }
}
