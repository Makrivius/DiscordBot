package discord.db;

import discord.Config;
import java.sql.*;

public class Database {
    private static final String URL = "jdbc:sqlite:" + Config.DB_PATH;

    static {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                System.out.println("Database loaded: " + URL);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Run any pending migrations before the bot starts.
        // Safe to call every startup — already-applied files are skipped.
        Migrations.run();
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}