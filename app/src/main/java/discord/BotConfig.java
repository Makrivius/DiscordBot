package discord;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord.util.PathHelper;
import io.github.cdimascio.dotenv.Dotenv;

public record BotConfig(String token, String defaultPrefix, int wsPort) {
}

public class BotConfig {
    public static void ensureLoaded() {
    }

    private static final String JAR_DIRECTORY = PathHelper.getJarDirectory();
    private static final File ENV_FILE = new File(JAR_DIRECTORY, ".env");

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(JAR_DIRECTORY)
            .ignoreIfMissing()
            .load();

    public static final String TOKEN = dotenv.get("DISCORD_TOKEN");
    public static final String PREFIX = dotenv.get("PREFIX", "!");
    public static final String MIGRATIONS_PATH = dotenv.get("MIGRATIONS_PATH", "migrations.sql");
    static {
        String LOG_LEVEL = dotenv.get("LOG_LEVEL", "INFO");
        System.setProperty("LOG_LEVEL", LOG_LEVEL);
    }
    public static final String DB_PATH = dotenv.get("DB_PATH", "bot.db");

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);
    static {
        log.info("JAR directory: {}", JAR_DIRECTORY);

        // Check if .env file actually exists
        if (!ENV_FILE.exists()) {
            log.warn(".env file NOT found in: {}", JAR_DIRECTORY);
        }

        // Check required fields
        if (TOKEN == null || TOKEN.isBlank()) {
            log.error("DISCORD_TOKEN is missing in .env");
            throw new IllegalStateException("Missing DISCORD_TOKEN in .env");
        }
    }
}
