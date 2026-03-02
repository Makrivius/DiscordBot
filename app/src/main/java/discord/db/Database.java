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
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
