package net.cc.core.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.CoreStanding;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.model.config.StorageConfig;
import net.cc.core.model.player.PaperCorePlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Controller class for data.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class DataController {

    private final CorePlugin plugin;

    private HikariDataSource dataSource;

    // SQL statements
    private String sqlSavePlayer;
    private String sqlConfirmAll;
    private String sqlUnconfirmAll;
    private String sqlQueryPlayerId;
    private String sqlQueryPlayerName;
    private String sqlQueryPlayers;
    private String sqlCreatePlayersTable;

    // Initializes the controller
    public void initialize() {
        final HikariConfig hikariConfig = this.getHikariConfig();

        // Attempt the initialize the database connection
        try {
            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (final HikariPool.PoolInitializationException e) {
            // Failed to connect, throw exception and disable plugin
            this.plugin.getComponentLogger().error("Error initializing database connection", e);
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return;
        }

        this.sqlSavePlayer = """
                INSERT INTO core_players
                (id, created_at, updated_at, joined_at, name, server, channels, standing, recent, display_name, nickname, friends, blocked, online, vanished, spying, allow_tpa, allow_mention, confirmed, coins, votes, meows)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE
                created_at = ?, updated_at = ?, joined_at = ?, name = ?, server = ?, channels = ?, standing = ?, recent = ?, display_name = ?, nickname = ?, friends = ?, blocked = ?, online = ?, vanished = ?, spying = ?, allow_tpa = ?, allow_mention = ?, confirmed = ?, coins = ?, votes = ?, meows = ?
                """;
        this.sqlConfirmAll = "UPDATE core_players SET confirmed = true;";
        this.sqlUnconfirmAll = "UPDATE core_players SET confirmed = false;";
        this.sqlQueryPlayerId = "SELECT * FROM core_players WHERE id = ?";
        this.sqlQueryPlayerName = "SELECT * FROM core_players WHERE name = ?";
        this.sqlQueryPlayers = "SELECT * FROM core_players";
        this.sqlCreatePlayersTable = """
                CREATE TABLE IF NOT EXISTS core_players (
                    id VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,
                    created_at LONG NOT NULL,
                    updated_at LONG NOT NULL,
                    joined_at LONG NOT NULL,
                    name TEXT NOT NULL,
                    server TEXT NOT NULL,
                    channels TEXT NOT NULL,
                    standing TEXT NOT NULL,
                    recent TEXT NOT NULL,
                    display_name TEXT NOT NULL,
                    nickname TEXT NOT NULL,
                    friends TEXT NOT NULL,
                    blocked TEXT NOT NULL,
                    online BOOLEAN NOT NULL,
                    vanished BOOLEAN NOT NULL,
                    spying BOOLEAN NOT NULL,
                    allow_tpa BOOLEAN NOT NULL,
                    allow_mention BOOLEAN NOT NULL,
                    confirmed BOOLEAN NOT NULL,
                    coins INTEGER NOT NULL,
                    votes INTEGER NOT NULL,
                    meows INTEGER NOT NULL
                );
                """;

        this.createPlayersTable();
    }

    // Gets a configured HikariConfig instance
    private @NotNull HikariConfig getHikariConfig() {
        final HikariConfig hikariConfig = new HikariConfig();
        final StorageConfig storageConfig = this.plugin.getConfigController().getStorageConfig();

        final String url = String.format(
                "jdbc:mysql://%s:%s/%s",
                storageConfig.getHost(),
                storageConfig.getPort(),
                storageConfig.getDatabase()
        );

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(storageConfig.getUsername());
        hikariConfig.setPassword(storageConfig.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMaximumPoolSize(10);
        return hikariConfig;
    }

    // Saves a CorePlayer to the database
    public CompletableFuture<Void> savePlayer(final CorePlayer corePlayer) {
        return CompletableFuture.runAsync(() -> {
            try {
                final String channels = CoreUtils.enumMapToString(corePlayer.getChannels());
                final String displayName = this.plugin.getMiniMessage().serialize(corePlayer.getDisplayName());
                final String nickname = this.plugin.getMiniMessage().serialize(corePlayer.getNickname());
                final String friends = CoreUtils.uuidListToString(corePlayer.getFriends());
                final String blocked = CoreUtils.uuidListToString(corePlayer.getBlocked());

                this.executeUpdate(this.sqlSavePlayer,
                        // Insert values
                        corePlayer.getUniqueId().toString(),
                        corePlayer.getCreatedAt(),
                        corePlayer.getUpdatedAt(),
                        corePlayer.getJoinedAt(),
                        corePlayer.getName(),
                        corePlayer.getServer().toString(),
                        channels,
                        corePlayer.getStanding().toString(),
                        corePlayer.getRecent().toString(),
                        displayName,
                        nickname,
                        friends,
                        blocked,
                        corePlayer.isOnline(),
                        corePlayer.isVanished(),
                        corePlayer.isSpying(),
                        corePlayer.isAllowTpa(),
                        corePlayer.isAllowMention(),
                        corePlayer.isConfirmed(),
                        corePlayer.getCoins(),
                        corePlayer.getVotes(),
                        corePlayer.getMeows(),
                        // Update values
                        corePlayer.getCreatedAt(),
                        corePlayer.getUpdatedAt(),
                        corePlayer.getJoinedAt(),
                        corePlayer.getName(),
                        corePlayer.getServer().toString(),
                        channels,
                        corePlayer.getStanding().toString(),
                        corePlayer.getRecent().toString(),
                        displayName,
                        nickname,
                        friends,
                        blocked,
                        corePlayer.isOnline(),
                        corePlayer.isVanished(),
                        corePlayer.isSpying(),
                        corePlayer.isAllowTpa(),
                        corePlayer.isAllowMention(),
                        corePlayer.isConfirmed(),
                        corePlayer.getCoins(),
                        corePlayer.getVotes(),
                        corePlayer.getMeows()
                );
            } catch (final SQLException e) {
                this.plugin.getComponentLogger().error("Error saving player '{}:{}'", corePlayer.getUniqueId(), corePlayer.getName(), e);
            }
        });
    }

    // Confirm all players in the database
    public CompletableFuture<Void> confirmAll() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.executeUpdate(this.sqlConfirmAll);
            } catch (final SQLException e) {
                this.plugin.getComponentLogger().error("Error confirming all players", e);
            }
        });
    }

    // Unconfirm all players in the database
    public CompletableFuture<Void> unconfirmAll() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.executeUpdate(this.sqlUnconfirmAll);
            } catch (final SQLException e) {
                this.plugin.getComponentLogger().error("Error unconfirming all players", e);
            }
        });
    }

    // Retrieves a CorePlayer from the database by ID
    public CompletableFuture<CorePlayer> queryPlayer(final UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.executeQuery(this.sqlQueryPlayerId,
                        resultSet -> resultSet.next()
                                ? this.parsePlayer(resultSet)
                                : null,
                        id.toString()
                );
            } catch (final SQLException e) {
                this.plugin.getComponentLogger().error("Error querying player '{}'", id, e);
                return null;
            }
        });
    }

    // Retrieves a CorePlayer from the database by username
    public CompletableFuture<CorePlayer> queryPlayer(final String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.executeQuery(this.sqlQueryPlayerName,
                        resultSet -> resultSet.next()
                                ? this.parsePlayer(resultSet)
                                : null,
                        name
                );
            } catch (final SQLException e) {
                this.plugin.getComponentLogger().error("Error querying player '{}'", name, e);
                return null;
            }
        });
    }

    // Retrieves all CorePlayer instances from the database
    public CompletableFuture<Set<CorePlayer>> queryPlayers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.executeQuery(this.sqlQueryPlayers,
                        this::parsePlayers
                );
            } catch (final SQLException e) {
                this.plugin.getComponentLogger().error("Error querying players", e);
                return null;
            }
        });
    }

    // Creates a set of CorePlayer instances from a result set
    private Set<CorePlayer> parsePlayers(final ResultSet resultSet) throws SQLException {
        final Set<CorePlayer> players = new HashSet<>();
        while (resultSet.next()) {
            players.add(this.parsePlayer(resultSet));
        }
        return players;
    }

    // Creates a CorePlayer from a result set
    private CorePlayer parsePlayer(final ResultSet resultSet) throws SQLException {
        final UUID id = UUID.fromString(resultSet.getString("id"));
        final long createdAt = resultSet.getLong("created_at");
        final long updatedAt = resultSet.getLong("updated_at");
        final long joinedAt = resultSet.getLong("joined_at");
        final String name = resultSet.getString("name");
        final CoreServer server = CoreServer.valueOf(resultSet.getString("server"));
        final EnumMap<CoreServer, CoreChannel> channels = CoreUtils.stringToEnumMap(resultSet.getString("channels"));
        final CoreStanding standing = CoreStanding.valueOf(resultSet.getString("standing"));
        final UUID recent = UUID.fromString(resultSet.getString("recent"));
        final Component displayName = this.plugin.getMiniMessage().deserialize(resultSet.getString("display_name"));
        final Component nickname = this.plugin.getMiniMessage().deserialize(resultSet.getString("nickname"));
        final List<UUID> friends = CoreUtils.stringToUuidList(resultSet.getString("friends"));
        final List<UUID> blocked = CoreUtils.stringToUuidList(resultSet.getString("blocked"));
        final boolean online = resultSet.getBoolean("vanished");
        final boolean vanished = resultSet.getBoolean("vanished");
        final boolean spying = resultSet.getBoolean("spying");
        final boolean allowTpa = resultSet.getBoolean("allow_tpa");
        final boolean allowMention = resultSet.getBoolean("allow_mention");
        final boolean confirmed = resultSet.getBoolean("confirmed");
        final int tokens = resultSet.getInt("tokens");
        final int votes = resultSet.getInt("votes");
        final int meows = resultSet.getInt("meows");

        return new PaperCorePlayer(
                this.plugin,
                id,
                createdAt,
                updatedAt,
                joinedAt,
                name,
                server,
                channels,
                standing,
                recent,
                displayName,
                nickname,
                friends,
                blocked,
                online,
                vanished,
                spying,
                allowTpa,
                allowMention,
                confirmed,
                tokens,
                votes,
                meows
        );
    }

    // Creates the players table if it does not exist
    private void createPlayersTable() {
        try {
            this.executeUpdate(this.sqlCreatePlayersTable);
        } catch (final SQLException e) {
            this.plugin.getComponentLogger().error("Error creating players table", e);
        }
    }

    // Attempts to close the database
    public void close() {
        try {
            this.dataSource.close();
        } catch (final RuntimeException e) {
            this.plugin.getComponentLogger().error("Error closing data connection", e);
        }
    }

    // Executes an SQL query and maps the result to a value
    private <T> @Nullable T executeQuery(final String query, final ThrowingFunction<ResultSet, T> mapper, final @Nullable Object... parameters) throws SQLException {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        preparedStatement.setObject(i + 1, parameters[i]);
                    }
                }
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    return ThrowingFunction.unchecked(mapper).apply(resultSet);
                }
            }
        }
    }

    // Executes an SQL update and returns the number of rows affected
    private void executeUpdate(final String query, final @Nullable Object... parameters) throws SQLException {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        preparedStatement.setObject(i + 1, parameters[i]);
                    }
                }
                preparedStatement.executeUpdate();
            }
        }
    }

    // A functional interface for a function that throws an SQLException
    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        static <T, R> ThrowingFunction<T, R> unchecked(final ThrowingFunction<T, R> f) {
            return f;
        }

        @Nullable
        R apply(T t) throws SQLException;
    }
}
