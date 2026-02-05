package net.cc.core.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CoreServer;
import net.cc.core.model.config.MeowConfig;
import net.cc.core.model.config.RedisConfig;
import net.cc.core.model.config.StorageConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for config files.
 *
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public final class ConfigController {

    private final CorePlugin plugin;

    private CommentedConfigurationNode primaryNode;
    private CommentedConfigurationNode messagesNode;

    // Initializes the controller
    public void initialize() {
        this.primaryNode = this.createNode(Constants.CONFIG_PRIMARY);
        this.messagesNode = this.createNode(Constants.CONFIG_MESSAGES);
    }

    // Gets a HoconConfigurationLoader for the given file path
    private HoconConfigurationLoader getHoconConfigurationLoader(final Path path) {
        return HoconConfigurationLoader.builder()
                .path(path)
                .prettyPrinting(true)
                .build();
    }

    // Creates a CommentedConfigurationNode for the given file
    private CommentedConfigurationNode createNode(final String fileName) {
        final Path path = this.plugin.getDataPath().resolve(fileName);

        if (Files.notExists(path)) {
            this.plugin.getComponentLogger().info("Config file '{}' not found, creating it", fileName);
            this.plugin.saveResource(fileName, false);
        }

        final var loader = this.getHoconConfigurationLoader(path);

        try {
            return loader.load();
        } catch (final ConfigurateException e) {
            this.plugin.getComponentLogger().error("Failed to load config file '{}'", fileName, e);
            return null;
        }
    }

    // Gets the server from the primary config
    public @NotNull CoreServer getCoreServer() {
        final String serverName = this.primaryNode.node("server").getString();
        try {
            if (serverName == null) return CoreServer.DEFAULT;
            return CoreServer.valueOf(serverName.toUpperCase());
        } catch (final IllegalArgumentException e) {
            this.plugin.getComponentLogger().error("Invalid server name '{}'", serverName, e);
            return CoreServer.DEFAULT;
        }
    }

    // Gets the debug mode state from the primary config
    public boolean isDebugMode() {
        return this.primaryNode.node("debug").getBoolean();
    }

    // Sets debug mode state in the primary config
    public void setDebugMode(final boolean debug) {
        try {
            this.primaryNode.node("debug").set(Boolean.class, debug);
            final Path path = this.plugin.getDataPath().resolve(Constants.CONFIG_PRIMARY);

            // Save config
            final var loader = this.getHoconConfigurationLoader(path);
            loader.save(this.primaryNode);
        } catch (final SerializationException e) {
            this.plugin.getComponentLogger().error("Failed to set debug mode to '{}'", debug, e);
        } catch (final ConfigurateException e) {
            this.plugin.getComponentLogger().error("Failed to save config", e);
        }
    }

    // Gets the storage config from the primary config
    public @NotNull StorageConfig getStorageConfig() {
        final var node = this.primaryNode.node("storage");

        try {
            final StorageConfig config = node.get(StorageConfig.class);
            return config != null
                    ? config
                    : new StorageConfig();
        } catch (SerializationException e) {
            this.plugin.getComponentLogger().error("Failed to load storage config, using default values", e);
            return new StorageConfig();
        }
    }

    // Gets the redis config from the primary config
    public @NotNull RedisConfig getRedisConfig() {
        final var node = this.primaryNode.node("redis");

        try {
            final RedisConfig config = node.get(RedisConfig.class);
            return config != null
                    ? config
                    : new RedisConfig();
        } catch (SerializationException e) {
            this.plugin.getComponentLogger().error("Failed to load redis config, using default values", e);
            return new RedisConfig();
        }
    }

    // Gets the meow config from the primary config
    public @NotNull MeowConfig getMeowConfig() {
        final var node = this.primaryNode.node("meow");

        try {
            final MeowConfig config = node.get(MeowConfig.class);
            return config != null
                    ? config
                    : new MeowConfig();
        } catch (SerializationException e) {
            this.plugin.getComponentLogger().error("Failed to load meow config, using default values", e);
            return new MeowConfig();
        }
    }

    // Gets the command prefix from the messages config
    public @NotNull String getPrefix() {
        final String prefix = this.messagesNode.node("prefix").getString();
        return prefix != null ? prefix : "";
    }

    // Gets a message from the message config
    public @NotNull Component getMessage(final String key, final TagResolver... resolvers) {
        final String message = this.messagesNode.node(key).getString();

        final List<TagResolver> tagResolvers = new ArrayList<>(List.of(resolvers));
        tagResolvers.add(Placeholder.parsed("prefix", this.getPrefix()));

        return message != null
                ? this.plugin.getMiniMessageFull().deserialize(message, tagResolvers.toArray(TagResolver[]::new))
                .decorationIfAbsent(
                        TextDecoration.ITALIC,
                        TextDecoration.State.FALSE
                )
                : Component.text(key);
    }

    // Gets a message list from the message config
    public @NotNull List<Component> getMessageList(final String key, final TagResolver... resolvers) {
        final List<Component> components = new ArrayList<>();

        try {
            final List<String> messages = this.messagesNode.node(key).getList(String.class);

            final List<TagResolver> tagResolvers = new ArrayList<>(List.of(resolvers));
            tagResolvers.add(Placeholder.parsed("prefix", this.getPrefix()));

            // Check if the list contains messages
            if (messages != null && !(messages.isEmpty())) {
                for (final String message : messages) {
                    // Add each message to the components list
                    components.add(MiniMessage.miniMessage().deserialize(
                                    message, tagResolvers.toArray(TagResolver[]::new))
                            .decorationIfAbsent(
                                    TextDecoration.ITALIC,
                                    TextDecoration.State.FALSE
                            ));
                }
            }
        } catch (final SerializationException e) {
            this.plugin.getComponentLogger().error("Failed to load messages from key '{}'", key, e);
        }

        return components;
    }
}
