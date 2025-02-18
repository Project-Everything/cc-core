package net.cc.core.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.cc.core.CorePlugin;
import net.cc.core.config.ConfigManager;
import net.cc.core.config.DatabaseSettings;
import net.cc.core.player.CorePlayer;
import net.cc.core.storage.query.CorePlayerQuery;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class DatabaseManager {

    private static final String PLAYERS_TABLE = "core_players";
    private final Logger logger;
    private final ConfigManager config;
    private HikariDataSource dataSource;

    // Constructor
    public DatabaseManager(final CorePlugin plugin) {
        this.logger = plugin.getLogger();
        this.config = plugin.getConfigManager();

        init();
        createTables();
    }

    // Method to init the HikariCP data source using the database settings from the config
    private void init() {
        final DatabaseSettings settings = config.getDatabaseSettings();
        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabase());
        hikariConfig.setUsername(settings.getUsername());
        hikariConfig.setPassword(settings.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariConfig.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(hikariConfig);
    }

    // Creates the necessary database tables if they do not exist
    private void createTables() {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.addBatch("CREATE TABLE IF NOT EXISTS " + PLAYERS_TABLE + " (id VARCHAR(36) PRIMARY KEY, username VARCHAR(16), display_name VARCHAR(128), nickname VARCHAR(64), vanished BOOLEAN, friends TEXT);");
            statement.executeBatch();
        } catch (SQLException e) {
            logger.severe("Error creating tables: " + e.getMessage());
        }
    }

    // Asynchronous method to save a CorePlayer to the database
    public void saveCorePlayer(final CorePlayer corePlayer) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                final PreparedStatement statement = connection.prepareStatement("INSERT INTO " + PLAYERS_TABLE + " (id, username, display_name, nickname, vanished, friends) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, display_name = ?, nickname = ?, vanished = ?, friends = ?;");

                // Insert values if missing
                statement.setString(1, corePlayer.getMojangId().toString());
                statement.setString(2, corePlayer.getUsername());
                statement.setString(3, corePlayer.getDisplayName());
                statement.setString(4, corePlayer.getNickname());
                statement.setBoolean(5, corePlayer.isVanished());
                statement.setString(6, listToString(corePlayer.getFriends()));
                // Update existing values
                statement.setString(7, corePlayer.getUsername());
                statement.setString(8, corePlayer.getDisplayName());
                statement.setString(9, corePlayer.getNickname());
                statement.setBoolean(10, corePlayer.isVanished());
                statement.setString(11, listToString(corePlayer.getFriends()));

                statement.execute();
            } catch (SQLException e) {
                logger.severe("Error saving core player: " + e.getMessage());
            }
        });
    }

    // Asynchronous method to query a CorePlayer from the database
    public CompletableFuture<CorePlayerQuery> queryCorePlayer(final UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            final CorePlayerQuery query = new CorePlayerQuery();
            try (Connection connection = getConnection()) {
                PreparedStatement statement;
                if (id != null) {
                    statement = connection.prepareStatement("SELECT * FROM " + PLAYERS_TABLE + " WHERE id = ?");
                    statement.setString(1, id.toString());
                } else {
                    statement = connection.prepareStatement("SELECT * FROM " + PLAYERS_TABLE + ";");
                }

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final UUID mojangId = UUID.fromString(resultSet.getString("id"));
                    final String username = resultSet.getString("username");
                    final String displayName = resultSet.getString("display_name");
                    final String nickname = resultSet.getString("nickname");
                    final boolean vanished = resultSet.getBoolean("vanished");
                    final List<String> friends = stringToList(resultSet.getString("friends"));

                    query.addResult(new CorePlayer(mojangId, username, displayName, nickname, vanished, friends));
                }
            } catch (SQLException e) {
                logger.severe("Error querying core player: " + e.getMessage());
            }
            return query;
        });
    }

    // Close the data source
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    // Retrieves a database connection from the data source
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // List to String conversion with null handling
    public static String listToString(final List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    // String to List conversion with null handling
    public static List<String> stringToList(final String str) {
        if (str == null || str.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(str.split(","));
    }
}
