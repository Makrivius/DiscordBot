package discord.db;

import java.sql.*;
import java.util.*;

public class DatabaseManager {

    // ─────────────────────────────────────────────
    // Generic CRUD
    // ─────────────────────────────────────────────

    /**
     * INSERT a row into any table.
     * columns and values must be the same length.
     *
     * Example:
     * insert("playlists", new String[]{"name","url","autoupdate"}, new
     * Object[]{"lofi","http://...",1});
     */
    public static void insert(String table, String[] columns, Object[] values) {
        String cols = String.join(", ", columns);
        String placeholders = "?,".repeat(columns.length);
        placeholders = placeholders.substring(0, placeholders.length() - 1);
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++)
                ps.setObject(i + 1, values[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insert failed on table: " + table, e);
        }
    }

    /**
     * UPDATE rows in any table.
     * setCols: columns to update
     * setValues: new values for those columns
     * whereCols: filter columns (AND-joined)
     * whereValues: filter values
     *
     * Example:
     * update("playlists", new String[]{"url"}, new Object[]{"http://new"},
     * new String[]{"name"}, new Object[]{"lofi"});
     */
    public static int update(String table, String[] setCols, Object[] setValues,
            String[] whereCols, Object[] whereValues) {
        String set = buildClause(setCols, ", ");
        String where = buildClause(whereCols, " AND ");
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + where;
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object v : setValues)
                ps.setObject(i++, v);
            for (Object v : whereValues)
                ps.setObject(i++, v);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update failed on table: " + table, e);
        }
    }

    /**
     * DELETE rows from any table.
     *
     * Example:
     * delete("playlists", new String[]{"name"}, new Object[]{"lofi"});
     */
    public static int delete(String table, String[] whereCols, Object[] whereValues) {
        String where = buildClause(whereCols, " AND ");
        String sql = "DELETE FROM " + table + " WHERE " + where;
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < whereValues.length; i++)
                ps.setObject(i + 1, whereValues[i]);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete failed on table: " + table, e);
        }
    }

    /**
     * SELECT rows from any table with optional WHERE filters.
     * Pass null/empty whereCols to get all rows.
     *
     * Example (filtered):
     * select("playlists", new String[]{"autoupdate"}, new Object[]{1});
     *
     * Example (all rows):
     * select("playlists", null, null);
     */
    public static List<Map<String, Object>> select(String table, String[] whereCols, Object[] whereValues) {
        boolean hasWhere = whereCols != null && whereCols.length > 0;
        String sql = "SELECT * FROM " + table + (hasWhere ? " WHERE " + buildClause(whereCols, " AND ") : "");
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasWhere)
                for (int i = 0; i < whereValues.length; i++)
                    ps.setObject(i + 1, whereValues[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++)
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("select failed on table: " + table, e);
        }
        return results;
    }

    /**
     * SELECT with a raw WHERE clause for complex queries.
     *
     * Example:
     * selectWhere("command_config", "date > ? AND guild_id = ?", new
     * Object[]{"2024-01-01", "123"});
     */
    public static List<Map<String, Object>> selectWhere(String table, String whereClause, Object[] values) {
        String sql = "SELECT * FROM " + table + " WHERE " + whereClause;
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (values != null)
                for (int i = 0; i < values.length; i++)
                    ps.setObject(i + 1, values[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++)
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("selectWhere failed on table: " + table, e);
        }
        return results;
    }

    /**
     * INSERT or REPLACE (upsert) — useful for tables with PRIMARY KEY constraints.
     *
     * Example:
     * upsert("playlists", new String[]{"name","url","autoupdate"}, new
     * Object[]{"lofi","http://...",1});
     */
    public static void upsert(String table, String[] columns, Object[] values) {
        String cols = String.join(", ", columns);
        String placeholders = "?,".repeat(columns.length);
        placeholders = placeholders.substring(0, placeholders.length() - 1);
        String sql = "INSERT OR REPLACE INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++)
                ps.setObject(i + 1, values[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("upsert failed on table: " + table, e);
        }
    }

    /**
     * Check if a row exists.
     *
     * Example:
     * exists("playlists", new String[]{"name"}, new Object[]{"lofi"});
     */
    public static boolean exists(String table, String[] whereCols, Object[] whereValues) {
        String where = buildClause(whereCols, " AND ");
        String sql = "SELECT 1 FROM " + table + " WHERE " + where + " LIMIT 1";
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < whereValues.length; i++)
                ps.setObject(i + 1, whereValues[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("exists check failed on table: " + table, e);
        }
    }

    /**
     * Execute a raw SQL statement (DDL, complex updates, etc.).
     *
     * Example:
     * execute("DELETE FROM playlists WHERE autoupdate = 0", null);
     */
    public static void execute(String sql, Object[] values) {
        try (Connection conn = Database.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (values != null)
                for (int i = 0; i < values.length; i++)
                    ps.setObject(i + 1, values[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("execute failed: " + sql, e);
        }
    }

    // ─────────────────────────────────────────────
    // Internal helper
    // ─────────────────────────────────────────────

    private static String buildClause(String[] cols, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String col : cols)
            joiner.add(col + " = ?");
        return joiner.toString();
    }
}