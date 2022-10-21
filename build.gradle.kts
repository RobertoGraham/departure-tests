plugins {
    java
    groovy
}

group = "io.github.robertograham"
version = "1.0-SNAPSHOT"
description = "Departure Tests"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("org.gebish:geb-spock:5.1")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.16.3"))
    testImplementation("org.testcontainers:spock")
    testImplementation("org.testcontainers:selenium")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:4.5.2")
    testImplementation("org.seleniumhq.selenium:selenium-support:4.1.1")
    testImplementation("org.slf4j:slf4j-simple:1.7.33")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging.events("passed", "skipped", "failed")
    }
}
