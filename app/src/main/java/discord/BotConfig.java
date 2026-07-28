package discord;

import io.github.cdimascio.dotenv.Dotenv;

public record BotConfig(String token, String clientId, String secret, String defaultPrefix, int wsPort, String dbPath,
        String youtubeRefreshToken) {
    public static BotConfig load() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String youtubeRefreshToken = dotenv.get("YOUTUBE_REFRESH_TOKEN", "");

        String token = require(dotenv.get("DISCORD_TOKEN"), "DISCORD_TOKEN");
        String clientId = require(dotenv.get("DISCORD_CLIENT_ID"), "DISCORD_CLIENT_ID");
        String secret = require(dotenv.get("DISCORD_CLIENT_SECRET"), "DISCORD_CLIENT_SECRET");

        String defaultPrefix = dotenv.get("PREFIX", "!");
        String serverPortEnv = System.getenv("SERVER_PORT");
        int wsPort = serverPortEnv != null
                ? Integer.parseInt(serverPortEnv)
                : Integer.parseInt(dotenv.get("WS_PORT", "8080"));
        Integer.parseInt(dotenv.get("WS_PORT", "8080"));
        String dbPath = dotenv.get("DB_PATH", "bot.db");

        return new BotConfig(token, clientId, secret, defaultPrefix, wsPort, dbPath, youtubeRefreshToken);
    }

    private static String require(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value;
    }
}
