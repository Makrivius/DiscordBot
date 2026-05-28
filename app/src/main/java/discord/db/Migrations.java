package discord.db;

import discord.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Runs schema.sql on startup when RUN_MIGRATIONS=true is set in .env
 *
 * schema.sql uses IF NOT EXISTS throughout, so it is safe to re-run
 * on every startup — it will never drop or overwrite existing data.
 *
 * To apply a schema change:
 *   1. Add the new CREATE/ALTER statement to schema.sql
 *   2. Set RUN_MIGRATIONS=true in .env
 *   3. Restart the bot — migrations run, then the bot starts normally
 *   4. Remove RUN_MIGRATIONS=true (or set to false) so it doesn't re-run
 */
public class Migrations {

    private static final String SCHEMA_FILE = "schema.sql";

    public static void run() {
        String flag = Config.RUN_MIGRATIONS;
        if (!"true".equalsIgnoreCase(flag)) {
            System.out.println("[Migration] Skipped (RUN_MIGRATIONS != true)");
            return;
        }

        System.out.println("[Migration] RUN_MIGRATIONS=true — applying schema.sql...");
        try (Connection conn = Database.get()) {
            String sql = load(SCHEMA_FILE);
            execute(conn, sql);
            System.out.println("[Migration] Schema applied successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Migration failed. Bot will not start.", e);
        }
    }

    private static String load(String filename) {
        InputStream is = Migrations.class.getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new RuntimeException("Schema file not found on classpath: " + filename);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read schema file: " + filename, e);
        }
    }

    private static void execute(Connection conn, String sql) throws SQLException {
        // Strip comment lines, then split on semicolons
        String stripped = Arrays.stream(sql.split("\n"))
                .filter(line -> !line.strip().startsWith("--"))
                .collect(Collectors.joining("\n"));

        try (Statement st = conn.createStatement()) {
            for (String stmt : stripped.split(";")) {
                String trimmed = stmt.strip();
                if (trimmed.isEmpty()) continue;
                try {
                    st.execute(trimmed);
                } catch (SQLException e) {
                    String msg = e.getMessage().toLowerCase();
                    if (msg.contains("already exists") || msg.contains("duplicate column")) {
                        System.out.println("[Migration] Skipping (already exists): "
                                + trimmed.substring(0, Math.min(60, trimmed.length())));
                    } else {
                        throw e;
                    }
                }
            }
        }
    }
}