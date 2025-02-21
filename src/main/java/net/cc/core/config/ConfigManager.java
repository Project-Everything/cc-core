package net.cc.core.config;

import net.cc.core.CorePlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.logging.Logger;

public final class ConfigManager {

    private final CorePlugin plugin;
    private final File dataFolder;
    private final Logger logger;
    private CommentedConfigurationNode root;

    // Constructor
    public ConfigManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.logger = plugin.getLogger();
    }

    // Method to create and load config file
    public void init() {
        File configFile = new File(dataFolder, "config.conf");
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(configFile.toPath())
                .build();

        if (!configFile.exists()) {
            plugin.saveResource("config.conf", false); // save config file if missing
            logger.info("Created config.conf");
        }

        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to get the server name from the config
    public String getServerName() {
        ConfigurationNode node = root.node("server");
        return node.getString("default");
    }

    // Method to get the database settings from the config
    public DatabaseSettings getDatabaseSettings() {
        ConfigurationNode node = root.node("database");
        try {
            return node.get(DatabaseSettings.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to get the redis settings from the config
    public RedisSettings getRedisSettings() {
        ConfigurationNode node = root.node("redis");
        try {
            return node.get(RedisSettings.class);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
