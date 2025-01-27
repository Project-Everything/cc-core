package net.cc.core.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.cc.core.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.logging.Logger;

public final class DatabaseManager {

    private final FileConfiguration config;
    private final Logger logger;
    private HikariDataSource dataSource;

    private static final String PLAYERS_TABLE = "core_players";

    public DatabaseManager(final CorePlugin plugin) {
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();

        init();
        createTables();
    }

    private void init() {
        final HikariConfig hikariConfig = new HikariConfig();

        final String host = config.getString("database.host");
        final int port = config.getInt("database.port");
        final String database = config.getString("database.database");
        final String username = config.getString("database.username");
        final String password = config.getString("database.password");

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariConfig.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(hikariConfig);
    }

    private void createTables() {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.addBatch("CREATE TABLE IF NOT EXISTS " + PLAYERS_TABLE + " (id VARCHAR(36) PRIMARY KEY, username VARCHAR(16), display_name VARCHAR(128), nickname VARCHAR(64), vanished BOOLEAN);");
            statement.executeBatch();
        } catch (SQLException e) {
            logger.severe("Error creating tables: " + e.getMessage());
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
