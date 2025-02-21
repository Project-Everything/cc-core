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
    public CorePlayerManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.redis = plugin.getRedisHandler();
        this.all = new ArrayList<>();
    }

    // Method to load a CorePlayer from the database and store in Redis
    public CorePlayer loadPlayer(Player player) {
        CorePlayer current = getPlayer(player);
        if (current != null) {
            current.setUsername(player.getName());
            updatePlayer(current);
            return current;
        }

        CorePlayer corePlayer = new CorePlayer(player);
        plugin.getDatabaseManager().queryCorePlayer(player.getUniqueId()).thenAccept(existing -> {
            if (existing != null) {
                corePlayer.setUsername(existing.getUsername());
                corePlayer.setDisplayName(existing.getDisplayName());
                corePlayer.setNickname(existing.getNickname());
                corePlayer.setFriends(existing.getFriends());
            } else {
                plugin.getDatabaseManager().saveCorePlayer(corePlayer);
            }
            updatePlayer(corePlayer);
        });

        return corePlayer;
    }

    // Get a CorePlayer from Bukkit Player object
    public CorePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    // Get a CorePlayer from UUID
    public CorePlayer getPlayer(UUID mojangId) {
        String key = "core:players:" + mojangId.toString();
        String value = redis.get(key);

        if (value == null) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(value, CorePlayer.class);
    }

    // Get a CorePlayer from Name (only use if desperate)
    public CorePlayer getPlayer(String name) {
        for (CorePlayer corePlayer : all) {
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
    public void updatePlayer(CorePlayer corePlayer) {
        String key = "core:players:" + corePlayer.getMojangId().toString();

        Gson gson = new Gson();
        String json = gson.toJson(corePlayer);

        redis.set(key, json, 5);
    }

    // Method to sync the list of all CorePlayer instances
    public void syncPlayerList(List<CorePlayer> players) {
        all.clear();
        all.addAll(players);
    }
}
