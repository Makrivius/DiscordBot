package discord.db;

import java.sql.*;

public class Schema {
    public static void init() {
        try (Connection conn = Database.get();
             Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS command_config (
                    name TEXT PRIMARY KEY,
                    guild_id TEXT NOT NULL,
                    url TEXT NOT NULL,
                    date TEXT NOT NULL
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS playlists (
                    name TEXT PRIMARY KEY,
                    url TEXT NOT NULL,
                    autoupdate INTEGER NOT NULL DEFAULT 1
                );
            """);

            // Future: privileges table
            // st.execute("CREATE TABLE IF NOT EXISTS privileges (...)");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
