import io.github.liplum.mindustry.minGameVersion

plugins {
    `maven-publish`
    java
    id("io.github.liplum.mgpp") version "1.3.1"
}
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository") }
        maven { url = uri("https://www.jitpack.io") }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_17
}

allprojects {
    group = "net.liplum"
    version = "1.9"
    
    buildscript {
        repositories {
            maven { url = uri("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository") }
            maven { url = uri("https://www.jitpack.io") }
        }
    }
    repositories {
        mavenCentral()
        maven { url = uri("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository") }
        maven { url = uri("https://www.jitpack.io") }
    }

    //force arc version
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if(this.requested.group == "com.github.Anuken.Arc") {
                    this.useVersion("v146")
                }
            }
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
        mindustry on "v146"
        arc on "v146"
    }
    client {
        mindustry official "v146"
    }
    server {
        mindustry official "v146"
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
        println("::set-output name=header::${rootProject.name} v$version on Mindustry v${mindustry.meta.minGameVersion}")
        println("::set-output name=version::v$version")
        try {
            val releases = uri("https://api.github.com/repos/liplum/MultiCrafterLib/releases").toURL().readText()
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
