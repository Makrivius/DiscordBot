package discord.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;

    /**
     * Initializes the DB with the path to the SQLite database file.
     * 
     * @param dbPath
     */
    public DatabaseManager(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    /**
     * Executes a query and processes the ResultSet with the provided handler.
     * 
     * @param <T>
     * @param sql
     * @param handler
     * @param params
     * @return The result from the handler after processing the ResultSet
     */
    public <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
        try (Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameters for the prepared statement
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            // Execute the query
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                return handler.handle(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes an update/insert/delete statement with the provided parameters.
     * 
     * @param sql
     * @param params
     */
    public void update(String sql, Object... params) {
        try (Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            // Execute the update
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Establishes a connection to the SQLite database.
     * 
     * @return
     * @throws SQLException
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(java.sql.ResultSet rs) throws SQLException;
    }
}
