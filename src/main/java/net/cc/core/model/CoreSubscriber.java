package net.cc.core.model;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.chat.ChatMessage;
import net.cc.core.api.model.chat.PrivateMessage;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.model.player.PaperCorePlayerMemento;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Model class for a Redis subscriber.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CoreSubscriber extends JedisPubSub {

    private final CorePlugin plugin;

    @Override
    public void onMessage(final String channel, final String message) {
        final Gson gson = new Gson();

        switch (channel) {
            // Redis channel for syncing player data between servers
            case Constants.REDIS_CHANNEL_SYNC -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                final CorePlayer corePlayer = gson.fromJson(message, PaperCorePlayerMemento.class).toCorePlayer(this.plugin);
                corePlayer.setUpdatedAt(System.currentTimeMillis(), true);
                this.plugin.getPlayerController().addPlayer(corePlayer);

                if (this.plugin.isDebugMode()) {
                    this.plugin.getComponentLogger().warn(
                            "Added '{}:{}' to the cache from REDIS_CHANNEL_SYNC",
                            corePlayer.getUniqueId(),
                            corePlayer.getName()
                    );
                }
            });
            // Redis channel for informing servers about players
            case Constants.REDIS_CHANNEL_UPDATE -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                final UUID uuid = UUID.fromString(message);
                final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(uuid);

                if (corePlayer != null) {
                    corePlayer.setUpdatedAt(System.currentTimeMillis(), true);
                } else {
                    this.plugin.getDataController().queryPlayer(uuid).thenAccept(dbPlayer -> {
                        if (dbPlayer != null) {
                            dbPlayer.setUpdatedAt(System.currentTimeMillis(), true);
                            dbPlayer.setOnline(true, true);
                            this.plugin.getPlayerController().addPlayer(dbPlayer);

                            if (this.plugin.isDebugMode()) {
                                this.plugin.getComponentLogger().warn(
                                        "Added '{}:{}' to the cache from REDIS_CHANNEL_UPDATE",
                                        dbPlayer.getUniqueId(),
                                        dbPlayer.getName()
                                );
                            }
                        } else {
                            this.plugin.getComponentLogger().error("Attempted to add '{}' to cache, but player does not exist.", uuid);
                        }
                    });
                }
            });
            // Redis channel for broadcasting messages
            case Constants.REDIS_CHANNEL_BROADCAST ->
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final TagResolver resolver = Placeholder.parsed("message", message);
                        this.plugin.getServer().forEachAudience(audience ->
                                audience.sendMessage(this.plugin.getConfigController().getMessage("command-broadcast", resolver)));
                    });
            // Redis channel for vanish notifications
            case Constants.REDIS_CHANNEL_VANISH -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                final String username = message.split(":")[0];
                final String staffMessage = message.split(":")[1];

                // Send message to online players
                for (final Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission(CorePermission.COMMAND_VANISH.get())
                            && !(username.equals(onlinePlayer.getName()))) {
                        onlinePlayer.sendMessage(this.plugin.getConfigController().getMessage(staffMessage,
                                Placeholder.parsed("player", username)));
                    }
                }
            });
            // Redis channel for chat messages
            case Constants.REDIS_CHANNEL_CHAT_MESSAGE ->
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
                        this.plugin.getChatController().handleChatMessage(chatMessage, false);
                    });
            // Redis channel for private messages
            case Constants.REDIS_CHANNEL_PRIVATE_MESSAGE ->
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final PrivateMessage privateMessage = gson.fromJson(message, PrivateMessage.class);
                        this.plugin.getChatController().handlePrivateMessage(privateMessage);
                    });
        }
    }

}
