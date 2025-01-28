package net.cc.core.player;

import com.google.gson.Gson;
import net.cc.core.CorePlugin;
import net.cc.core.storage.RedisManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class CorePlayerManager {

    private final CorePlugin plugin;
    private final RedisManager redis;

    public CorePlayerManager(final CorePlugin plugin) {
        this.plugin = plugin;
        this.redis = plugin.getRedisHandler();
    }

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
            } else {
                plugin.getDatabaseManager().saveCorePlayer(corePlayer);
            }
            updatePlayer(corePlayer);
        });

        return corePlayer;
    }

    public CorePlayer getPlayer(final Player player) {
        return getPlayer(player.getUniqueId());
    }

    public CorePlayer getPlayer(final UUID mojangId) {
        final String key = "core:players:" + mojangId.toString();
        final String value = redis.get(key);

        if (value == null) {
            return null;
        }

        final Gson gson = new Gson();
        return gson.fromJson(value, CorePlayer.class);
    }

    public void updatePlayer(final CorePlayer corePlayer) {
        final String key = "core:players:" + corePlayer.getMojangId().toString();

        final Gson gson = new Gson();
        final String json = gson.toJson(corePlayer);

        redis.set(key, json, 5);
    }
}
