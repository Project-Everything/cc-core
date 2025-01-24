package net.cc.core.player;

import net.cc.core.CorePlugin;
import net.cc.core.storage.RedisManager;

public final class CorePlayerManager {

    private final CorePlugin plugin;
    private final RedisManager redis;

    public CorePlayerManager(final CorePlugin plugin) {
        this.plugin = plugin;
        this.redis = plugin.getRedisHandler();
    }
}
