package discord.db;

import java.sql.*;
import java.util.List;

public class GenericDao {

    public void save(String table, List<String> columns, List<Object> values) {
        if (columns.size() != values.size()) {
            throw new IllegalArgumentException("Columns and values size mismatch");
        }

        String cols = String.join(", ", columns);
        String placeholders = String.join(", ", columns.stream().map(c -> "?").toList());

        String sql = "INSERT OR REPLACE INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";

        try (Connection conn = Database.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
