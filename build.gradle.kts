import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    application

    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "no.kommune.oslo"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Logging
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    // Argparser
    implementation("com.github.ajalt.clikt:clikt:3.1.0")

    // Http client
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.0")

    // Serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")

    // JWT handling
    implementation(group = "com.nimbusds", name = "nimbus-jose-jwt", version = "9.7")

    // Testing
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "no.kommune.oslo.maskinporten.cli.MainKt"
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "no.kommune.oslo.maskinporten.cli.MainKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
