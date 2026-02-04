package discord;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Tools.PathHelper;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private static final String JAR_DIRECTORY = PathHelper.getJarDirectory();
    private static final File ENV_FILE = new File(JAR_DIRECTORY, ".env");

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(JAR_DIRECTORY)
            .ignoreIfMissing()
            .load();

    public static final String TOKEN = dotenv.get("DISCORD_TOKEN");
    public static final String PREFIX = dotenv.get("PREFIX", "!");

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
