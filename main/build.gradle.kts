@file:Suppress("SpellCheckingInspection")

import java.io.ByteArrayOutputStream

plugins {
    java
}
val OutputJarName: String by project
val MindustryVersion: String by project
val ArcVersion: String by project
val PlumyVersion: String by project
val OpenGalVersion: String by project
val sdkRoot: String? by extra(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT"))
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":lib"))
    compileOnly("com.github.Anuken.Arc:arc-core:$ArcVersion")
    compileOnly("com.github.anuken.mindustryjitpack:core:$MindustryVersion")
    testImplementation("com.github.anuken.mindustryjitpack:core:$MindustryVersion")
    testImplementation("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
    // annotationProcessor "com.github.Anuken:jabel:$jabelVersion"
}

tasks {
    register("jarAndroid") {
        group = "build"
        dependsOn("jar")

        doLast {
            val sdkRoot = sdkRoot
            if (sdkRoot == null || !File(sdkRoot).exists())
                throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
            val platformRoot = File("$sdkRoot/platforms/").listFiles()!!.sorted().reversed()
                .find { f -> File(f, "android.jar").exists() }
                ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")
            //collect dependencies needed for desugaring
            val allDependencies = configurations.compileClasspath.get().toList() +
                    configurations.runtimeClasspath.get().toList() +
                    listOf(File(platformRoot, "android.jar"))
            val dependencies = allDependencies.joinToString(" ") { "--classpath ${it.path}" }
            //dex and desugar files - this requires d8 in your PATH
            val paras = "$dependencies --min-api 14 --output ${OutputJarName}Android.jar ${OutputJarName}Desktop.jar"
            try {
                exec {
                    commandLine = "d8 $paras".split(' ')
                    workingDir = File("$buildDir/libs")
                    standardOutput = System.out
                    errorOutput = System.err
                }
            } catch (_: Exception) {
                val cmdOutput = ByteArrayOutputStream()
                logger.lifecycle("d8 cannot be found in your PATH, so trying to use an absolute path.")
                exec {
                    commandLine = listOf("where", "d8")
                    standardOutput = cmdOutput
                    errorOutput = System.err
                }
                val d8FullPath = cmdOutput.toString().replace("\r", "").replace("\n", "")
                exec {
                    commandLine = "$d8FullPath $paras".split(' ')
                    workingDir = File("$buildDir/libs")
                    standardOutput = System.out
                    errorOutput = System.err
                }
            }
        }
    }

    register<Jar>("deployLocal") {
        group = "build"
        dependsOn("jarAndroid")
        archiveFileName.set("${OutputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${OutputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${OutputJarName}Android.jar")
        )

        doLast {
            delete {
                delete("$buildDir/libs/${OutputJarName}Android.jar")
            }
        }
    }

    register<Jar>("deploy") {
        group = "build"
        dependsOn("jarAndroid")
        archiveFileName.set("${OutputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${OutputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${OutputJarName}Android.jar")
        )

        doLast {
            delete {
                delete(
                    "$buildDir/libs/${OutputJarName}Desktop.jar",
                    "$buildDir/libs/${OutputJarName}Android.jar"
                )
            }
        }
    }
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${OutputJarName}Desktop.jar")
    includeEmptyDirs = false
    exclude("**/**/*.java")
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )

    from(rootDir) {
        // add something into your Jar
        include("mod.hjson")
        include("icon.png")
    }

    from("$projectDir/assets") {
        include("**")
    }
}