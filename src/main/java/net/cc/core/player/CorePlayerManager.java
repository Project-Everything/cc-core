package net.cc.core.player;

import com.google.gson.Gson;
import net.cc.core.CorePlugin;
import net.cc.core.storage.RedisManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CorePlayerManager {

    private final CorePlugin plugin;
    private final RedisManager redis;
    private final List<CorePlayer> all;

    // Constructor
    public CorePlayerManager(final CorePlugin plugin) {
        this.plugin = plugin;
        this.redis = plugin.getRedisHandler();
        this.all = new ArrayList<>();
    }

    // Method to load a CorePlayer from the database and store in Redis
    public CorePlayer loadPlayer(final Player player) {
        final CorePlayer current = getPlayer(player);
        if (current != null) {
            current.setUsername(player.getName());
            updatePlayer(current);
            return current;
        }

        final CorePlayer corePlayer = new CorePlayer(player);
        plugin.getDatabaseManager().queryCorePlayer(player.getUniqueId()).thenAccept(corePlayerQuery -> {
            if (corePlayerQuery.hasResults()) {
                final CorePlayer existing = corePlayerQuery.getFirst();
                corePlayer.setUsername(existing.getUsername());
                corePlayer.setDisplayName(existing.getDisplayName());
                corePlayer.setNickname(existing.getNickname());
                corePlayer.setVanished(existing.isVanished());
                corePlayer.setFriends(existing.getFriends());
            } else {
                plugin.getDatabaseManager().saveCorePlayer(corePlayer);
            }
            updatePlayer(corePlayer);
        });

        return corePlayer;
    }

    // Get a CorePlayer from Bukkit Player object
    public CorePlayer getPlayer(final Player player) {
        return getPlayer(player.getUniqueId());
    }

    // Get a CorePlayer from UUID
    public CorePlayer getPlayer(final UUID mojangId) {
        final String key = "core:players:" + mojangId.toString();
        final String value = redis.get(key);

        if (value == null) {
            return null;
        }

        final Gson gson = new Gson();
        return gson.fromJson(value, CorePlayer.class);
    }

    // Get a CorePlayer from Name (only use if desperate)
    public CorePlayer getPlayer(final String name) {
        for (final CorePlayer corePlayer : all) {
            if (corePlayer.getUsername().equals(name)) {
                return corePlayer;
            }
        }
        return null;
    }

    // Method to get a list of all CorePlayer instances
    public List<CorePlayer> getPlayers() {
        return all;
    }

    // Method to update a CorePlayer in Redis
    public void updatePlayer(final CorePlayer corePlayer) {
        final String key = "core:players:" + corePlayer.getMojangId().toString();

        final Gson gson = new Gson();
        final String json = gson.toJson(corePlayer);

        redis.set(key, json, 5);
    }

    // Method to sync the list of all CorePlayer instances
    public void syncPlayerList(final List<CorePlayer> players) {
        all.clear();
        all.addAll(players);
    }
}
