plugins {
    java
}
val MindustryVersion: String by project
val ArcVersion: String by project
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":lib"))
    compileOnly("com.github.anuken.mindustryjitpack:core:$MindustryVersion")
    compileOnly("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
    // annotationProcessor "com.github.Anuken:jabel:$jabelVersion"
}