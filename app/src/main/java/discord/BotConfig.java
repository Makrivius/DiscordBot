package discord;

import io.github.cdimascio.dotenv.Dotenv;

public record BotConfig(String token, String defaultPrefix, int wsPort, String dbPath) {
    public static BotConfig load() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // System.out.println("CWD: " + System.getProperty("user.dir"));
        // System.out.println("Token found: " + (dotenv.get("DISCORD_TOKEN") != null));

        String token = require(dotenv.get("DISCORD_TOKEN"), "DISCORD_TOKEN");
        String defaultPrefix = dotenv.get("PREFIX", "!");
        int wsPort = Integer.parseInt(dotenv.get("WS_PORT", "8080"));
        String dbPath = dotenv.get("DB_PATH", "bot.db");

        return new BotConfig(token, defaultPrefix, wsPort, dbPath);
    }

    private static String require(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value;
    }
}
