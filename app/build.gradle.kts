plugins {
    application
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.ben-manes.versions") version "0.53.0"
}

// Global Dependency Versions
object Versions {
    const val jda = "6.3.0"
    const val javaWebSocket = "1.5.6"
    const val reflections = "0.10.2"
    const val slf4j = "2.0.13"
    const val logbackClassic = "1.5.6"
    const val javaDotenv = "5.2.2"
    const val gson = "2.11.0"
    const val lavaPlayer = "2.2.6"
    const val lavalinkYoutube = "1.18.0"
    const val jdaVea = "0.1.5"
    const val jacksonBom = "2.17.0"
    const val sqliteJdbc = "3.51.2.0"
}

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
}

dependencies {
    // JDA & Audio Natives
    implementation("club.minnced:jdave-api:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-linux-x86-64:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-linux-aarch64:${Versions.jdaVea}")
    implementation("club.minnced:jdave-native-win-x86-64:${Versions.jdaVea}")
    implementation("net.dv8tion:JDA:${Versions.jda}")
    implementation("org.java-websocket:Java-WebSocket:${Versions.javaWebSocket}")

    // Audio Player & Plugins
    implementation("dev.arbjerg:lavaplayer:${Versions.lavaPlayer}")
    implementation("dev.lavalink.youtube:common:${Versions.lavalinkYoutube}")

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