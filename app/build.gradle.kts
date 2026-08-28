plugins {
    application
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

// Global Dependency Versions
object Versions {
    const val jda = "latest.release"
    const val gson = "latest.release"
    const val lavaLink = "latest.release"
    const val lavaPlayer = "latest.release"
    const val lavalinkYoutube = "latest.release"
    const val lavaSrc = "latest.release"
    const val jdaVea = "latest.release"
    const val jacksonBom = "latest.release"
    const val sqliteJdbc = "latest.release"
}

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
    maven("https://maven.topi.wtf/releases")
}

dependencies {
    // Spring framework
	implementation ("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JDA & Audio Natives
    implementation("club.minnced:jdave-api:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-linux-x86-64:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-linux-aarch64:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-win-x86-64:${Versions.jdaVea}")
    implementation("net.dv8tion:JDA:${Versions.jda}")

    // Audio Player & Plugins
    implementation("dev.arbjerg:lavalink-client:${Versions.lavaLink}")
    implementation("dev.arbjerg:lavaplayer:${Versions.lavaPlayer}")
    implementation("dev.lavalink.youtube:common:${Versions.lavalinkYoutube}")
    implementation("com.github.topi314.lavasrc:lavasrc:${Versions.lavaSrc}")
    implementation("com.github.topi314.lavasrc:protocol-jvm:${Versions.lavaSrc}")
    // Reflection & Utilities
    implementation("com.google.code.gson:gson:${Versions.gson}")

    // Logging Framework
    implementation("org.slf4j:slf4j-api")
    implementation("ch.qos.logback:logback-classic:")

    // Database
    implementation("org.xerial:sqlite-jdbc:${Versions.sqliteJdbc}")

    // Jackson JSON Toolkit
    implementation(platform("com.fasterxml.jackson:jackson-bom:${Versions.jacksonBom}"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("discord.App")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Spring block
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}