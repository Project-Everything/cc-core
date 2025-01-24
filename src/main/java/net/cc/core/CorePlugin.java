package net.cc.core;

import net.cc.core.storage.DatabaseManager;
import net.cc.core.storage.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CorePlugin extends JavaPlugin {

    private RedisManager redisManager;
    private DatabaseManager databaseManager;

    @Override
    public void onLoad() {
        // Plugin load logic
        redisManager = new RedisManager(this);
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        databaseManager = new DatabaseManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (redisManager != null) {
            redisManager.close();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public RedisManager getRedisHandler() {
        return redisManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
