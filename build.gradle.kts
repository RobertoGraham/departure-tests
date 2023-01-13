plugins {
    java
    groovy
}

group = "io.github.robertograham"
version = "1.0-SNAPSHOT"
description = "Departure Tests"
java.sourceCompatibility = JavaVersion.VERSION_19

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.slf4j:slf4j-simple:2.0.5")
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("com.microsoft.playwright:playwright:1.28.1")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging.events("passed", "skipped", "failed")
    }
}
