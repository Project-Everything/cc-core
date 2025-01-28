package net.cc.core.task;

import com.google.gson.Gson;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.storage.RedisManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class UpdateRedisTask implements Runnable {

    private final CorePlugin plugin;
    private final RedisManager redis;

    public UpdateRedisTask(final CorePlugin plugin) {
        this.plugin = plugin;
        this.redis = plugin.getRedisHandler();
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
            if (corePlayer != null) {
                plugin.getCorePlayerManager().updatePlayer(corePlayer);
            }
        }

        final String key = "core:players:*";
        final List<String> values = redis.getValues(key);

        if (values == null || values.isEmpty()) {
            return;
        }

        final List<CorePlayer> players = new ArrayList<>();
        for (final String value : values) {
            final Gson gson = new Gson();
            players.add(gson.fromJson(value, CorePlayer.class));
        }

        plugin.getCorePlayerManager().syncPlayerList(players);
    }
}
