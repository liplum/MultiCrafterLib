import io.github.liplum.mindustry.genModHjson
import io.github.liplum.mindustry.importMindustry

plugins {
    java
    `maven-publish`
    id("io.github.liplum.mgpp")
}
repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}
sourceSets {
    main {
        java.srcDir("src")
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
    }
}

dependencies {
    importMindustry()
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
    // For Too many items: https://github.com/EB-wilson/TooManyItems
    compileOnly("com.github.EB-wilson:TooManyItems:2.2")
}

java {
    withSourcesJar()
    withJavadocJar()
}
mindustryAssets{
    // no icon
    icon at "$projectDir/icon.png"
}
tasks.genModHjson {
    // no mod.hjson
    enabled = false
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}