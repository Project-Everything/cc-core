package net.cc.core.controller;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.model.player.PaperCorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller class for players.
 *
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public final class PlayerController {

    private final CorePlugin plugin;
    private final Map<UUID, CorePlayer> players = new ConcurrentHashMap<>();

    // Initializes the controller
    public void initialize() {
        this.plugin.getDataController().queryPlayers().thenAccept(dbPlayers -> {
            for (final CorePlayer dbPlayer : dbPlayers) {
                this.players.put(dbPlayer.getUniqueId(), dbPlayer);
            }
        });
    }

    // Loads a player from the database on server join
    public CompletableFuture<CorePlayer> loadPlayer(final Player player) {
        final String username = player.getName();

        // Check if the player already exists
        if (this.players.containsKey(player.getUniqueId())) {
            final CorePlayer corePlayer = this.players.get(player.getUniqueId());
            corePlayer.setServer(this.plugin.getCoreServer());

            if (!(corePlayer.isOnline())) {
                // Player was offline
                corePlayer.setOnline(true, false);
                corePlayer.setUpdatedAt(System.currentTimeMillis(), false);
                corePlayer.setJoinedAt(System.currentTimeMillis());
            }

            // Update username
            this.updateUsername(corePlayer, username);

            return CompletableFuture.completedFuture(corePlayer);
        }

        // Query database for player
        return this.plugin.getDataController().queryPlayer(player.getUniqueId()).thenCompose(dbPlayer -> {
            if (dbPlayer != null) {
                // Update username
                this.updateUsername(dbPlayer, username);

                // Player was found in the database
                dbPlayer.setName(username);
                dbPlayer.setUpdatedAt(System.currentTimeMillis(), false);
                dbPlayer.setJoinedAt(System.currentTimeMillis());
                dbPlayer.setServer(this.plugin.getCoreServer());

                this.validateChannels(dbPlayer);
                this.addPlayer(dbPlayer);
                return CompletableFuture.completedFuture(dbPlayer);
            } else {
                // Player was not found in the database
                final CorePlayer corePlayer = new PaperCorePlayer(this.plugin, player);

                return plugin.getDataController().savePlayer(corePlayer).thenApply(v -> {
                    this.addPlayer(corePlayer);
                    return corePlayer;
                });
            }
        }).exceptionally(e -> {
            // Exception thrown during load
            this.plugin.getComponentLogger().error(
                    "Error loading CorePlayer for '{}:{}'",
                    player.getUniqueId(), username, e
            );
            return null;
        });
    }

    // Gets a CorePlayer by a UUID
    public CorePlayer getPlayer(final UUID uuid) {
        return this.players.get(uuid);
    }

    // Gets a CorePlayer from a Bukkit Player
    public CorePlayer getPlayer(final Player player) {
        return this.players.get(player.getUniqueId());
    }

    // Gets a CorePlayer from a username
    public CorePlayer getPlayer(final String username) {
        for (final CorePlayer corePlayer : this.players.values()) {
            if (corePlayer.getName().equals(username)) {
                return corePlayer;
            }
        }
        return null;
    }

    // Gets a CorePlayer asynchronously by a UUID
    public CompletableFuture<CorePlayer> getPlayerAsync(final UUID uuid) {
        final CorePlayer corePlayer = this.getPlayer(uuid);

        if (corePlayer != null) {
            return CompletableFuture.completedFuture(corePlayer);
        } else {
            return plugin.getDataController().queryPlayer(uuid);
        }
    }

    // Gets a CorePlayer asynchronously by a username
    public CompletableFuture<CorePlayer> getPlayerAsync(final String username) {
        final CorePlayer corePlayer = this.getPlayer(username);

        if (corePlayer != null) {
            return CompletableFuture.completedFuture(corePlayer);
        } else {
            return plugin.getDataController().queryPlayer(username);
        }
    }

    // Gets a collection of all cached CorePlayer objects
    public Collection<CorePlayer> getPlayers() {
        return this.players.values();
    }

    // Adds a CorePlayer to the cache
    public void addPlayer(final CorePlayer corePlayer) {
        this.players.put(corePlayer.getUniqueId(), corePlayer);
    }

    // Sends a player update message to other servers
    public void updatePlayer(final CorePlayer corePlayer) {
        this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_UPDATE, corePlayer.getUniqueId().toString());
    }

    // Sends a player sync message to other servers
    public void syncPlayer(final PaperCorePlayer corePlayer) {
        final Gson gson = new Gson();

        this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_SYNC,
                gson.toJson(corePlayer.createMemento(this.plugin)));
    }

    // Confirm all players
    public boolean confirmAll() {
        // Set value for all online players
        for (final CorePlayer corePlayer : this.players.values()) {
            corePlayer.setConfirmed(true, true);
        }

        // Update all players in database
        return !(this.plugin.getDataController().confirmAll().isCompletedExceptionally());
    }

    // Unconfirm all players
    public boolean unconfirmAll() {
        // Set value for all online players
        for (final CorePlayer corePlayer : this.players.values()) {
            corePlayer.setConfirmed(false, true);
        }

        // Update all players in database
        return !(this.plugin.getDataController().unconfirmAll().isCompletedExceptionally());
    }

    // Gets the player count
    public int getPlayerCount(final Player player) {
        int count = 0;
        for (final CorePlayer onlineCorePlayer : this.getPlayers()) {
            if (!(onlineCorePlayer.isOnline())) continue;
            if (onlineCorePlayer.isVanished() && !(player.hasPermission(CorePermission.COMMAND_VANISH.get()))) continue;
            count++;
        }
        return count;
    }

    // Sets vanish state for a player based on their current vanish state
    public void vanishPlayer(final CorePlayer corePlayer, final Player player) {
        this.vanishPlayer(corePlayer, player, corePlayer.isVanished());
    }

    // Sets vanish state for a player to a specified state
    public void vanishPlayer(final CorePlayer corePlayer, final Player player, final boolean vanished) {
        String vanishMessage;
        String staffMessage;

        if (!(vanished)) {
            // Enable vanish for player
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, true));
            vanishMessage = "command-vanish-enable";
            staffMessage = "command-vanish-enable-notify";
            corePlayer.setVanished(true);
        } else {
            // Disable vanish for player
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            vanishMessage = "command-vanish-disable";
            staffMessage = "command-vanish-disable-notify";
            corePlayer.setVanished(false);
        }

        player.sendMessage(this.plugin.getConfigController().getMessage(vanishMessage));

        // Publish isVanished message to Redis
        this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_VANISH, String.format("%s:%s", player.getName(), staffMessage));
    }

    // Gets a custom Player component from a Bukkit Player
    public Component getPlayerComponent(final Player player) {
        final CorePlayer corePlayer = this.getPlayer(player);
        final ClickEvent clickEvent = ClickEvent.suggestCommand("/msg " + player.getName());

        if (corePlayer != null) {
            // Core player found
            final String dateString = this.getDateString(corePlayer.getCreatedAt());

            final HoverEvent<Component> hoverEvent = HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                    this.plugin.getConfigController().getMessage("placeholder-player-hover",
                            Placeholder.parsed("date", dateString)));

            return corePlayer.getDisplayName()
                    .hoverEvent(hoverEvent);
        } else {
            // No core player
            return player.displayName()
                    .clickEvent(clickEvent);
        }
    }

    // Gets a custom Player tab component from a Bukkit Player
    public Component getPlayerTabComponent(final Player player) {
        final CorePlayer corePlayer = this.getPlayer(player);

        if (corePlayer != null) {
            // Check if player is vanished
            if (corePlayer.isVanished()) {
                return this.plugin.getConfigController().getMessage("placeholder-vanished",
                        Placeholder.component("name", corePlayer.getDisplayName()));
            } else {
                return corePlayer.getDisplayName();
            }
        }

        return player.displayName();
    }

    // Gets a custom Player component from a CorePlayer
    public Component getPlayerComponent(final CorePlayer corePlayer) {
        final String dateString = this.getDateString(corePlayer.getCreatedAt());
        final ClickEvent clickEvent = ClickEvent.suggestCommand("/msg " + corePlayer.getName());

        final HoverEvent<Component> hoverEvent = HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                this.plugin.getConfigController().getMessage("placeholder-player-hover",
                        Placeholder.parsed("date", dateString)));

        if (corePlayer.isVanished()) {
            // Player is vanished
            return this.plugin.getConfigController().getMessage("placeholder-vanished",
                            Placeholder.component("name", corePlayer.getDisplayName()))
                    .hoverEvent(hoverEvent)
                    .clickEvent(clickEvent);
        } else {
            // Player is not vanished
            return corePlayer.getDisplayName()
                    .hoverEvent(hoverEvent)
                    .clickEvent(clickEvent);
        }
    }

    // Gets a date string for a Linux time
    public String getDateString(final long date) {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    // Validate channel map for a player
    public void validateChannels(final CorePlayer corePlayer) {
        for (final CoreServer server : CoreServer.values()) {
            // Check if server is missing from map
            if (!(corePlayer.getChannels().containsKey(server))) {
                // Add default channel to map
                corePlayer.setChannel(server, server.getDefaultChannel());
            }

            final Player player = this.plugin.getServer().getPlayer(corePlayer.getUniqueId());
            if (player != null) {
                // Check if player has permission for the channel
                if (!(player.hasPermission(corePlayer.getChannel(server).getPermission().get()))) {
                    // Set channel to default channel
                    corePlayer.setChannel(server, server.getDefaultChannel());
                }
            }
        }
    }

    // Get a list of players with the highest coins
    public List<CorePlayer> getTopCoins() {
        return this.getPlayers()
                .stream()
                .filter(corePlayer -> corePlayer.getCoins() != 0)
                .sorted(
                        // Primary sort by coins
                        Comparator.comparing(CorePlayer::getCoins).reversed()
                                // Secondary sort by name
                                .thenComparing(CorePlayer::getName)
                )
                .toList();
    }

    // Get a list of players with the highest votes
    public List<CorePlayer> getTopVotes() {
        return this.getPlayers()
                .stream()
                .filter(corePlayer -> corePlayer.getVotes() != 0)
                .sorted(
                        // Primary sort by votes
                        Comparator.comparing(CorePlayer::getVotes).reversed()
                                // Secondary sort by name
                                .thenComparing(CorePlayer::getName)
                )
                .toList();
    }

    // Get a list of players with the highest meows
    public List<CorePlayer> getTopMeows() {
        return this.getPlayers()
                .stream()
                .filter(corePlayer -> corePlayer.getMeows() != 0)
                .sorted(
                        // Primary sort by meows
                        Comparator.comparing(CorePlayer::getMeows).reversed()
                                // Secondary sort by name
                                .thenComparing(CorePlayer::getName)
                )
                .toList();
    }

    // Updates a player's username
    private void updateUsername(final CorePlayer corePlayer, final String username) {
        // Check if username matches
        if (!(corePlayer.getName().equals(username))) {
            // Username does not match, update name
            corePlayer.setName(username);
            corePlayer.setDisplayName(this.plugin.getConfigController().getMessage("default-display-name",
                    Placeholder.parsed("username", username)));
        }
    }
}
