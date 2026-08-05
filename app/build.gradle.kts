plugins {
    application
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.ben-manes.versions") version "0.53.0"
}

// Global Dependency Versions
object Versions {
    const val jda = "latest.release"
    const val javalin = "latest.release"
    const val reflections = "latest.release"
    const val slf4j = "latest.release"
    const val logbackClassic = "latest.release"
    const val javaDotenv = "latest.release"
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
    // JDA & Audio Natives
    implementation("club.minnced:jdave-api:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-linux-x86-64:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-linux-aarch64:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-win-x86-64:${Versions.jdaVea}")
    implementation("net.dv8tion:JDA:${Versions.jda}")
    implementation("io.javalin:javalin:${Versions.javalin}")

    // Audio Player & Plugins
    implementation("dev.arbjerg:lavalink-client:${Versions.lavaLink}")
    implementation("dev.arbjerg:lavaplayer:${Versions.lavaPlayer}")
    implementation("dev.lavalink.youtube:common:${Versions.lavalinkYoutube}")
    implementation("com.github.topi314.lavasrc:lavasrc:${Versions.lavaSrc}")
    implementation("com.github.topi314.lavasrc:protocol-jvm:${Versions.lavaSrc}")
    // Reflection & Utilities
    implementation("org.reflections:reflections:${Versions.reflections}")
    implementation("io.github.cdimascio:java-dotenv:${Versions.javaDotenv}")
    implementation("com.google.code.gson:gson:${Versions.gson}")

    // Logging Framework
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("ch.qos.logback:logback-classic:${Versions.logbackClassic}")

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