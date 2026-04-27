package discord.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs SQL migration files in order, exactly once each.
 *
 * Files live in resources/migrations/ and must be named:
 *   V001__description.sql
 *   V002__description.sql
 *   ...
 *
 * Applied migrations are recorded in the `schema_version` table so they
 * never run twice, even across restarts.
 *
 * Call Migrations.run() once, early in your startup — before any other
 * DB access.
 */
public class Migrations {

    // All migration filenames in the exact order they must run.
    // Add new entries at the bottom — never reorder or rename existing ones.
    private static final String[] FILES = {
            "V001__initial_schema.sql",
            "V002__add_image_pools.sql",
    };

    private static final String MIGRATIONS_DIR = "migrations/";

    public static void run() {
        try (Connection conn = Database.get()) {
            ensureVersionTable(conn);
            List<String> applied = getApplied(conn);

            for (String file : FILES) {
                if (applied.contains(file)) {
                    System.out.println("[Migration] Already applied: " + file);
                    continue;
                }

                System.out.println("[Migration] Applying: " + file);
                String sql = load(file);
                execute(conn, sql);
                markApplied(conn, file);
                System.out.println("[Migration] Done: " + file);
            }

        } catch (Exception e) {
            // Hard stop — never start the bot with a broken schema
            throw new RuntimeException("Migration failed. Bot will not start.", e);
        }
    }

    // -------------------------------------------------------------------------

    private static void ensureVersionTable(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    filename   TEXT NOT NULL PRIMARY KEY,
                    applied_at TEXT NOT NULL DEFAULT (datetime('now'))
                )
            """);
        }
    }

    private static List<String> getApplied(Connection conn) throws SQLException {
        List<String> applied = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT filename FROM schema_version")) {
            while (rs.next()) applied.add(rs.getString("filename"));
        }
        return applied;
    }

    private static void markApplied(Connection conn, String filename) throws SQLException {
        try (var ps = conn.prepareStatement(
                "INSERT INTO schema_version (filename) VALUES (?)")) {
            ps.setString(1, filename);
            ps.executeUpdate();
        }
    }

    private static String load(String filename) {
        String path = MIGRATIONS_DIR + filename;
        InputStream is = Migrations.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new RuntimeException("Migration file not found on classpath: " + path);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read migration file: " + path, e);
        }
    }

    private static void execute(Connection conn, String sql) throws SQLException {
        // Split on semicolons so multi-statement files work correctly
        String[] statements = sql.split(";");
        try (Statement st = conn.createStatement()) {
            for (String stmt : statements) {
                String trimmed = stmt.strip();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    st.execute(trimmed);
                }
            }
        }
    }
}