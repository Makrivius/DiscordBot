package discord.db;

import java.sql.*;

public class Schema {
    public static void init() {
        try (Connection conn = Database.get();
             Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS command_config (
                    name        TEXT PRIMARY KEY,
                    channel_id  TEXT NOT NULL,
                    image_url   TEXT NOT NULL,
                    date_from   TEXT NOT NULL,
                    date_to     TEXT NOT NULL,
                    owner_id    TEXT NOT NULL
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS playlists (
                    name        TEXT PRIMARY KEY,
                    url         TEXT NOT NULL,
                    autoupdate  INTEGER NOT NULL DEFAULT 1,
                    owner_id    TEXT NOT NULL
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS privileges (
                    role_id     TEXT NOT NULL,
                    guild_id    TEXT NOT NULL,
                    level       TEXT NOT NULL,
                    PRIMARY KEY (role_id, guild_id)
                );
            """);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}