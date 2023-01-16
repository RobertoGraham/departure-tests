plugins {
    java
    groovy
    application
}

application {
    mainClass.set("com.microsoft.playwright.CLI")
}

group = "io.github.robertograham"
version = "1.0-SNAPSHOT"
description = "Departure Tests"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.microsoft.playwright:playwright:1.28.1")
    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
