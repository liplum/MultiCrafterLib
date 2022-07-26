plugins {
    kotlin("jvm") version "1.6.21"
    groovy
    java
}
buildscript{
    dependencies{
        classpath(kotlin("gradle-plugin", version = "1.6.21"))
    }
}
repositories {
    mavenCentral()
    maven {
        url = uri("https://www.jitpack.io")
    }
}
sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("test")
    }
}
dependencies {
    implementation(gradleApi())
    implementation("net.lingala.zip4j:zip4j:2.11.1")
    implementation("com.google.code.gson:gson:2.9.0")
}