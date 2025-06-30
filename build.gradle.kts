import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

configurations.all {
    exclude(group = "commons-logging", module = "commons-logging")
}

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "2.1.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "com.spaceoverview"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    val dropwizardVersion = "4.0.14"
    val jdbiVersion = "3.49.4"
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
    implementation("com.google.inject:guice:7.0.0")
    implementation("commons-validator:commons-validator:1.9.0")
    implementation("io.dropwizard:dropwizard-auth:$dropwizardVersion")
    implementation("io.dropwizard:dropwizard-core:$dropwizardVersion")
    implementation("org.flywaydb:flyway-core:11.9.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.9.1")
    implementation("org.jdbi:jdbi3-core:$jdbiVersion")
    implementation("org.jdbi:jdbi3-kotlin-sqlobject:$jdbiVersion")
    implementation("org.jdbi:jdbi3-kotlin:$jdbiVersion")
    implementation("org.jdbi:jdbi3-postgres:$jdbiVersion")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.springframework.security:spring-security-web:6.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "2048m"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveFileName.set("space.jar")
        mergeServiceFiles()
        exclude(listOf("META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.SF"))
        manifest {
            attributes(mapOf("Main-Class" to "SpaceApplicationKt"))
        }
    }
}
