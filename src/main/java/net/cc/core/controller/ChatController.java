package net.cc.core.controller;

import com.google.gson.Gson;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.chat.ChatMessage;
import net.cc.core.api.model.chat.PrivateMessage;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller class for chat.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ChatController {

    private final CorePlugin plugin;

    // Send a chat message
    public void sendChatMessage(final CorePlayer sender, final CoreChannel channel, final String message) {
        final UUID uuid = sender.getUniqueId();
        final CoreServer server = sender.getServer();

        final Player player = this.plugin.getServer().getPlayer(uuid);
        if (player == null) {
            return;
        }

        // Check if player has permission
        final String permission = channel.getPermission().get();
        if (!(player.hasPermission(permission))) {
            player.sendMessage(this.plugin.getConfigController().getMessage("error-channel-permission"));
            return;
        }

        switch (channel) {
            case PLOTS_LOCAL_CHAT -> {
                final Plot plot = this.plugin.getServiceController().getPlot(sender.getUniqueId());

                // Check if player is in a plot
                if (plot == null) {
                    player.sendMessage(this.plugin.getConfigController().getMessage("error-no-plot"));
                    return;
                }

                // Check if player is alone on the plot
                if (this.isAloneOnPlot(plot, uuid)) {
                    player.sendMessage(this.plugin.getConfigController().getMessage("error-nearby-players"));
                }
            }
            case EARTH_TOWN_CHAT -> {
                final Resident senderResident = this.plugin.getServiceController().getResident(uuid);

                // Check if sender is in a town
                if (senderResident == null || !(senderResident.hasTown())) {
                    player.sendMessage(this.plugin.getConfigController().getMessage("error-no-town"));
                    return;
                }
            }
            case EARTH_NATION_CHAT -> {
                final Resident senderResident = this.plugin.getServiceController().getResident(uuid);

                // Check if sender is in a nation
                if (senderResident == null || !(senderResident.hasNation())) {
                    player.sendMessage(this.plugin.getConfigController().getMessage("error-no-nation"));
                    return;
                }
            }
        }

        final String prefix = this.plugin.getServiceController().getPrefix(uuid);
        final String key = "channel-" + channel.getKey();
        final boolean color = this.plugin.getServiceController().hasPermission(uuid,
                CorePermission.CHAT_COLOR.get());

        final Component messageComponent = color
                ? this.plugin.getMiniMessage().deserialize(message)
                : PlainTextComponentSerializer.plainText().deserialize(message);

        final String nickname = this.plugin.getMiniMessageStrict().serialize(sender.getNickname());
        final String fullNickname = nickname.isEmpty() || nickname.equals("<!italic></!italic>")
                ? ""
                : " <yellow>(" + nickname + ")</yellow>";

        final Component component = this.plugin.getConfigController().getMessage(key,
                Placeholder.parsed("username", sender.getName()),
                Placeholder.component("display_name", sender.getDisplayName()),
                Placeholder.parsed("nickname", fullNickname),
                Placeholder.parsed("user_prefix", prefix),
                Placeholder.parsed("server", server.toString()),
                Placeholder.parsed("message", this.plugin.getMiniMessage().serialize(messageComponent)),
                Placeholder.parsed("role", this.plugin.getServiceController().getTownyTitle(uuid))
        );

        // Send chat message
        final String formattedMessage = this.plugin.getMiniMessage().serialize(component);
        final ChatMessage chatMessage = new ChatMessage(
                sender.getUniqueId(),
                channel,
                server,
                System.currentTimeMillis(),
                message,
                formattedMessage
        );

        final Gson gson = new Gson();
        final String gsonMessage = gson.toJson(chatMessage);

        this.handleChatMessage(chatMessage, true);
        this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_CHAT_MESSAGE, gsonMessage);
    }

    // Sends a private chat message
    public void sendPrivateMessage(final CorePlayer sender, final CorePlayer target, final String message) {
        final CoreServer server = sender.getServer();
        final boolean color = this.plugin.getServiceController().hasPermission(target.getUniqueId(),
                CorePermission.COMMAND_MESSAGE_COLOR.get());

        final Component temp = color
                ? this.plugin.getMiniMessage().deserialize(message)
                : PlainTextComponentSerializer.plainText().deserialize(message);

        final Component component = this.plugin.getConfigController().getMessage("command-message-receive",
                Placeholder.parsed("username", sender.getName()),
                Placeholder.parsed("server", sender.getServer().toString()),
                Placeholder.parsed("message", this.plugin.getMiniMessage().serialize(temp))
        );

        // Send private chat message
        final String formattedMessage = this.plugin.getMiniMessage().serialize(component);
        final PrivateMessage privateMessage = new PrivateMessage(
                sender.getUniqueId(),
                target.getUniqueId(),
                server,
                System.currentTimeMillis(),
                message,
                formattedMessage
        );

        final Gson gson = new Gson();
        final String gsonMessage = gson.toJson(privateMessage);

        this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_PRIVATE_MESSAGE, gsonMessage);
    }

    // Handles a chat message
    public void handleChatMessage(final ChatMessage message, final boolean local) {
        final CoreServer server = message.server();
        final CoreChannel channel = message.channel();
        final Component component = this.plugin.getMiniMessage().deserialize(message.formattedMessage());
        final String permission = channel.getPermission().get();

        // Check if message has already been handled
        if (!(local) && this.plugin.getCoreServer().equals(server)) {
            return;
        }

        // Check if message can be sent on this server
        if (!(this.plugin.getCoreServer().equals(server)) && !(channel.isCrossServer())) {
            this.plugin.getServer().getOnlinePlayers().forEach(viewingPlayer -> {
                final CorePlayer senderCorePlayer = this.plugin.getPlayerController().getPlayer(message.sender());
                final CorePlayer viewingCorePlayer = this.plugin.getPlayerController().getPlayer(viewingPlayer);

                // Send spy message
                if (viewingCorePlayer.isSpying() && viewingPlayer.hasPermission(permission)) {
                    this.sendChatSpyMessage(senderCorePlayer, viewingPlayer, message.message());
                }
            });
            return;
        }

        this.plugin.getServer().getOnlinePlayers().forEach(viewingPlayer -> {
            final CorePlayer viewingCorePlayer = this.plugin.getPlayerController().getPlayer(viewingPlayer);
            boolean visible = false;

            if (viewingPlayer.hasPermission(permission)) {
                // Handle message
                switch (channel) {
                    case PLOTS_LOCAL_CHAT -> {
                        final Plot senderPlot = this.plugin.getServiceController().getPlot(message.sender());
                        final Plot viewerPlot = this.plugin.getServiceController().getPlot(viewingPlayer.getUniqueId());

                        // Check if plots exist
                        if (senderPlot != null && viewerPlot != null) {
                            // Check if players are in the same plot
                            if (senderPlot.getId().equals(viewerPlot.getId())) {
                                // Send message
                                visible = true;
                            }
                        }
                    }
                    case EARTH_LOCAL_CHAT -> {
                        final Player player = this.plugin.getServer().getPlayer(message.sender());
                        if (player != null) {
                            final Location playerLocation = player.getLocation();
                            final Location targetLocation = viewingPlayer.getLocation();

                            // Check if player is in the same world
                            if (playerLocation.getWorld() == targetLocation.getWorld()) {
                                // Check if player is within the radius
                                if (playerLocation.distance(targetLocation) < Constants.LOCAL_CHAT_RADIUS) {
                                    // Send message
                                    visible = true;
                                }
                            }
                        }
                    }
                    case EARTH_TOWN_CHAT -> {
                        final Resident senderResident = this.plugin.getServiceController().getResident(message.sender());
                        final Resident targetResident = this.plugin.getServiceController().getResident(viewingCorePlayer.getUniqueId());

                        // Check if residents exist
                        if (senderResident != null && targetResident != null) {
                            final Town senderTown = this.plugin.getServiceController().getTown(senderResident);
                            final Town targetTown = this.plugin.getServiceController().getTown(targetResident);

                            // Check if towns exist
                            if (senderTown != null && targetTown != null) {
                                // Check if residents are in the same town
                                if (senderTown.getUUID().equals(targetTown.getUUID())) {
                                    // Send message
                                    visible = true;
                                }
                            }
                        }
                    }
                    case EARTH_NATION_CHAT -> {
                        final Resident senderResident = this.plugin.getServiceController().getResident(message.sender());
                        final Resident targetResident = this.plugin.getServiceController().getResident(viewingCorePlayer.getUniqueId());

                        // Check if residents exist
                        if (senderResident != null && targetResident != null) {
                            final Nation senderNation = this.plugin.getServiceController().getNation(senderResident);
                            final Nation targetNation = this.plugin.getServiceController().getNation(targetResident);

                            // Check if nations exist
                            if (senderNation != null && targetNation != null) {
                                // Check if residents are in the same nation
                                if (senderNation.getUUID().equals(targetNation.getUUID())) {
                                    // Send message
                                    visible = true;
                                }
                            }
                        }
                    }
                    default -> visible = true;
                }

                if (viewingPlayer.hasPermission(permission) && visible) {
                    // Send message to player
                    if (viewingCorePlayer.getBlocked().contains(message.sender())) {
                        // Send blocked message
                        viewingPlayer.sendMessage(this.plugin.getConfigController().getMessage("channel-blocked",
                                Placeholder.component("message", component))
                        );
                    } else {
                        // Send regular message
                        viewingPlayer.sendMessage(component);
                    }
                } else {
                    final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(message.sender());

                    // Send spy message
                    if (viewingPlayer.hasPermission(permission) && viewingCorePlayer.isSpying()) {
                        this.sendChatSpyMessage(corePlayer, viewingPlayer, message.message());
                    }
                }
            }
        });

        // Send message to console
        this.plugin.getServer().getConsoleSender().sendMessage(component);
    }

    // Handles a private message
    public void handlePrivateMessage(final PrivateMessage message) {
        final CorePlayer senderCorePlayer = this.plugin.getPlayerController().getPlayer(message.sender());
        final CorePlayer targetCorePlayer = this.plugin.getPlayerController().getPlayer(message.target());
        final Component component = this.plugin.getMiniMessage().deserialize(message.formattedMessage());

        // Send message to console
        this.plugin.getServer().getConsoleSender().sendMessage(component);

        // Send message to target
        final Player targetPlayer = this.plugin.getServer().getPlayer(targetCorePlayer.getUniqueId());
        if (targetPlayer != null) {
            if (targetCorePlayer.getBlocked().contains(message.sender())) {
                targetPlayer.sendMessage(this.plugin.getConfigController().getMessage("channel-blocked",
                        Placeholder.component("message", component))
                );
                return;
            } else {
                targetPlayer.sendMessage(component);
            }
        }

        this.plugin.getServer().getOnlinePlayers().forEach(viewingPlayer -> {
            if (viewingPlayer.getUniqueId().equals(targetCorePlayer.getUniqueId())) {
                // Skip target player
                return;
            }

            final CorePlayer viewingCorePlayer = this.plugin.getPlayerController().getPlayer(viewingPlayer);

            // Send spy message
            if (viewingCorePlayer.isSpying()) {
                this.sendPrivateChatSpyMessage(senderCorePlayer, targetCorePlayer, viewingPlayer, message.message());
            }
        });
    }

    // Sends a chat spy message
    public void sendChatSpyMessage(final CorePlayer corePlayer, final Player viewingPlayer, final String message) {
        final boolean canSpy = !(viewingPlayer.getUniqueId().equals(corePlayer.getUniqueId()));

        if (canSpy) {
            viewingPlayer.sendMessage(this.plugin.getConfigController().getMessage("channel-spy",
                    Placeholder.parsed("username", corePlayer.getName()),
                    Placeholder.parsed("message", message)
            ));
        }
    }

    // Sends a private chat spy message
    public void sendPrivateChatSpyMessage(final CorePlayer corePlayer, final CorePlayer targetCorePlayer, final Player viewingPlayer, final String message) {
        final boolean canSpy = !(viewingPlayer.getUniqueId().equals(corePlayer.getUniqueId()))
                && !(viewingPlayer.getUniqueId().equals(targetCorePlayer.getUniqueId()));

        if (canSpy) {
            viewingPlayer.sendMessage(this.plugin.getConfigController().getMessage("channel-spy-private",
                    Placeholder.parsed("sender", corePlayer.getName()),
                    Placeholder.parsed("target", targetCorePlayer.getName()),
                    Placeholder.parsed("message", message)
            ));
        }
    }

    // Returns true if a player is alone on a plot
    private boolean isAloneOnPlot(final Plot plot, final UUID uuid) {
        final List<UUID> uuids = new ArrayList<>();

        for (final PlotPlayer<?> plotPlayer : plot.getPlayersInPlot()) {
            final CorePlayer plotCorePlayer = this.plugin.getPlayerController().getPlayer(plotPlayer.getUUID());
            if (plotCorePlayer != null) {
                final boolean canSee = this.plugin.getServiceController().hasPermission(uuid, CorePermission.COMMAND_VANISH.get());

                if (plotCorePlayer.isVanished() && !(canSee)) {
                    // Plot player is vanished
                    continue;
                }
                uuids.add(plotPlayer.getUUID());
            }
        }

        return uuids.size() < 2;
    }
}
