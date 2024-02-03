plugins {
    kotlin("jvm") version "1.9.21"
    groovy
    java
}
buildscript{
    dependencies{
        classpath(kotlin("gradle-plugin", version = "1.9.21"))
    }
}
repositories {
    mavenCentral()
    maven { url = uri("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository") }
    maven { url = uri("https://www.jitpack.io") }
}
sourceSets {
    main {
        java.srcDir("src")
    }
}
dependencies {
    implementation(gradleApi())
    implementation("net.lingala.zip4j:zip4j:2.11.1")
    implementation("com.google.code.gson:gson:2.9.0")
}