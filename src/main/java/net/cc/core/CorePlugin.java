package net.cc.core;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.cc.core.command.*;
import net.cc.core.config.ConfigManager;
import net.cc.core.hook.PlaceholderAPIHook;
import net.cc.core.task.*;
import net.cc.core.listener.PlayerListener;
import net.cc.core.player.CorePlayerManager;
import net.cc.core.storage.DatabaseManager;
import net.cc.core.storage.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("UnstableApiUsage")
public final class CorePlugin extends JavaPlugin {

    private String serverName;

    private ConfigManager configManager;
    private RedisManager redisManager;
    private DatabaseManager databaseManager;
    private CorePlayerManager corePlayerManager;

    @Override
    public void onLoad() {
        // Plugin load logic
        configManager = new ConfigManager(this);
        serverName = configManager.getServerName();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        redisManager = new RedisManager(this);
        databaseManager = new DatabaseManager(this);
        corePlayerManager = new CorePlayerManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        registerCommands();
        registerListeners();
        registerTasks();
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

    public String getServerName() {
        return serverName;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RedisManager getRedisHandler() {
        return redisManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CorePlayerManager getCorePlayerManager() {
        return corePlayerManager;
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            new DisplayNameCommand(this, commands);
            new NicknameCommand(this, commands);
            new VanishCommand(this, commands);
        });
    }

    private void registerListeners() {
        new PlayerListener(this);
    }

    private void registerTasks() {
        final BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.runTaskTimer(this, new UpdatePlayersTask(this), 60000L, 60000L); // update every minute
        scheduler.runTaskTimer(this, new UpdateVanishTask(this), 10L, 10L); // update every 10ms
        scheduler.runTaskTimer(this, new UpdateRedisTask(this), 10L, 10L); // update every 10ms
    }
}
