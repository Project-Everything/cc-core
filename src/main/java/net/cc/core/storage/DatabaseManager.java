package net.cc.core.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.cc.core.CorePlugin;
import net.cc.core.config.ConfigManager;
import net.cc.core.config.DatabaseSettings;
import net.cc.core.player.CorePlayer;
import net.cc.core.storage.query.CorePlayerQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class DatabaseManager {

    private final Logger logger;
    private final ConfigManager config;
    private HikariDataSource dataSource;

    private static final String PLAYERS_TABLE = "core_players";

    public DatabaseManager(final CorePlugin plugin) {
        this.logger = plugin.getLogger();
        this.config = plugin.getConfigManager();

        init();
        createTables();
    }

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

    private void createTables() {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.addBatch("CREATE TABLE IF NOT EXISTS " + PLAYERS_TABLE + " (id VARCHAR(36) PRIMARY KEY, username VARCHAR(16), display_name VARCHAR(128), nickname VARCHAR(64), vanished BOOLEAN);");
            statement.executeBatch();
        } catch (SQLException e) {
            logger.severe("Error creating tables: " + e.getMessage());
        }
    }

    public CompletableFuture<Void> saveCorePlayer(final CorePlayer corePlayer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                final PreparedStatement statement = connection.prepareStatement("INSERT INTO " + PLAYERS_TABLE + " (id, username, display_name, nickname, vanished) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, display_name = ?, nickname = ?, vanished = ?;");
                // insert
                statement.setString(1, corePlayer.getMojangId().toString());
                statement.setString(2, corePlayer.getUsername());
                statement.setString(3, corePlayer.getDisplayName());
                statement.setString(4, corePlayer.getNickname());
                statement.setBoolean(5, corePlayer.isVanished());
                // update
                statement.setString(6, corePlayer.getUsername());
                statement.setString(7, corePlayer.getDisplayName());
                statement.setString(8, corePlayer.getNickname());
                statement.setBoolean(9, corePlayer.isVanished());
            } catch (SQLException e) {
                logger.severe("Error saving core player: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<CorePlayerQuery> queryCorePlayer(final UUID mojangId) {
        return CompletableFuture.supplyAsync(() -> {
            final CorePlayerQuery query = new CorePlayerQuery();
            try (Connection connection = getConnection()) {
                final PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + PLAYERS_TABLE + " WHERE id = ?;");
                statement.setString(1, mojangId.toString());

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final String username = resultSet.getString("username");
                    final String displayName = resultSet.getString("display_name");
                    final String nickname = resultSet.getString("nickname");
                    final boolean vanished = resultSet.getBoolean("vanished");
                    final List<UUID> friends = new ArrayList<>();

                    query.addResult(new CorePlayer(mojangId, username, displayName, nickname, vanished, friends));
                }
            } catch (SQLException e) {
                logger.severe("Error querying core player: " + e.getMessage());
            }
            return query;
        });
    }

    public CompletableFuture<CorePlayerQuery> queryCorePlayers() {
        return CompletableFuture.supplyAsync(() -> {
            final CorePlayerQuery query = new CorePlayerQuery();
            try (Connection connection = getConnection()) {
                final PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + PLAYERS_TABLE + ";");

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final UUID mojangId = UUID.fromString(resultSet.getString("id"));
                    final String username = resultSet.getString("username");
                    final String displayName = resultSet.getString("display_name");
                    final String nickname = resultSet.getString("nickname");
                    final boolean vanished = resultSet.getBoolean("vanished");
                    final List<UUID> friends = new ArrayList<>();

                    query.addResult(new CorePlayer(mojangId, username, displayName, nickname, vanished, friends));
                }
            } catch (SQLException e) {
                logger.severe("Error querying core players: " + e.getMessage());
            }
            return query;
        });
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
