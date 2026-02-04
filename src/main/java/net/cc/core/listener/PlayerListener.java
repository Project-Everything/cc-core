package net.cc.core.listener;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Listener class for Player events.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public final class PlayerListener implements Listener {

    private final CorePlugin plugin;
    private final Map<UUID, CompletableFuture<Boolean>> awaitingResponse = new ConcurrentHashMap<>();

    // Registers the listener
    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    // Handle the player join event
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.joinMessage(null);

        final Player player = event.getPlayer();

        // Handle join process for player
        this.plugin.getPlayerController().loadPlayer(player).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                if (!(corePlayer.isConfirmed())) {
                    // Show confirmation dialog
                    this.showDialog(player, corePlayer);
                }

                this.plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
                    if (!(corePlayer.isVanished())) {
                        // Send join message
                        onlinePlayer.sendMessage(this.plugin.getConfigController().getMessage("server-join-message",
                                Placeholder.component("player", corePlayer.getDisplayName())));
                    }
                });
            } else {
                this.plugin.getComponentLogger().error(
                        "Failed to load CorePlayer for '{}:{}' when handling join process",
                        player.getUniqueId(), player.getName()
                );
            }
        });
    }

    // Handle the player quit event
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        event.quitMessage(null);

        final Player player = event.getPlayer();
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

        // Check for and cancel the awaiting response future
        final CompletableFuture<Boolean> future = this.awaitingResponse.remove(player.getUniqueId());
        if (future != null) {
            future.cancel(true);
        }

        // Handle quit process for player
        if (corePlayer != null) {
            this.plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
                if (!corePlayer.isVanished()) {
                    // Send quit message
                    onlinePlayer.sendMessage(this.plugin.getConfigController().getMessage("server-quit-message",
                            Placeholder.component("player", corePlayer.getDisplayName())));
                }
            });

            // Save player to database
            this.plugin.getDataController().savePlayer(corePlayer);
        } else {
            this.plugin.getComponentLogger().error("Failed to retrieve CorePlayer for '{}' when handling quit process", player.getName());
        }

        this.awaitingResponse.remove(player.getUniqueId());
    }

    // Handle the asynchronous chat event
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(final AsyncChatEvent event) {
        event.setCancelled(true);

        final Player player = event.getPlayer();
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

        if (corePlayer != null) {
            // Handle chat message
            final CoreChannel channel = corePlayer.getChannel(this.plugin.getCoreServer());
            final String message = this.plugin.getMiniMessage().serialize(event.message());
            this.plugin.getChatController().sendChatMessage(corePlayer, channel, message);
        } else {
            this.plugin.getComponentLogger().error("Failed to retrieve CorePlayer for '{}' when handling chat message", player.getName());
        }
    }

    // Handle the dialog button click event
    @EventHandler
    public void onHandleDialog(final PlayerCustomClickEvent event) {
        // Handle custom click only for configuration connection
        if (!(event.getCommonConnection() instanceof PlayerGameConnection connection)) {
            return;
        }

        final UUID id = connection.getPlayer().getUniqueId();
        final Key key = event.getIdentifier();

        if (key.equals(Constants.KEY_DIALOG_JOIN_ACCEPT)) {
            // Set dialog result to true
            this.setDialogResult(id, true);
        } else if (key.equals(Constants.KEY_DIALOG_JOIN_DECLINE)) {
            // Set dialog result to false
            this.setDialogResult(id, false);
        }
    }

    // Set the dialog result
    private void setDialogResult(final UUID id, final boolean value) {
        final CompletableFuture<Boolean> future = awaitingResponse.get(id);
        if (future != null) {
            future.complete(value);
        }
    }

    // Show the join dialog to the player
    private void showDialog(final Player player, final CorePlayer corePlayer) {
        // Create the dialog
        final Component title = this.plugin.getConfigController().getMessage("dialog-join-title");
        final List<Component> messages = this.plugin.getConfigController().getMessageList("dialog-join-messages");
        final Component accept = this.plugin.getConfigController().getMessage("dialog-join-accept");
        final Component acceptTooltip = this.plugin.getConfigController().getMessage("dialog-join-accept-tooltip");
        final Component decline = this.plugin.getConfigController().getMessage("dialog-join-decline");
        final Component declineTooltip = this.plugin.getConfigController().getMessage("dialog-join-decline-tooltip");

        final List<DialogBody> body = new ArrayList<>();
        for (final Component message : messages) {
            body.add(DialogBody.plainMessage(message));
        }

        final Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(false)
                        .body(body)
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.builder(accept)
                                .tooltip(acceptTooltip)
                                .action(DialogAction.customClick(Constants.KEY_DIALOG_JOIN_ACCEPT, null))
                                .build(),
                        ActionButton.builder(decline)
                                .tooltip(declineTooltip)
                                .action(DialogAction.customClick(Constants.KEY_DIALOG_JOIN_DECLINE, null))
                                .build()
                )));

        // Create async dialog instance
        final CompletableFuture<Boolean> response = new CompletableFuture<>();
        response.completeOnTimeout(false, 1, TimeUnit.MINUTES);
        this.awaitingResponse.put(player.getUniqueId(), response);

        // Show dialog
        player.showDialog(dialog);

        // Handle dialog response
        response.thenAccept(accepted -> {
            if (response.isCancelled()) {
                // Future was canceled, do nothing
                return;
            }

            if (accepted) {
                // Confirm player
                corePlayer.setConfirmed(true, false);

                // Run player message logic on the main thread
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    // Check if player is still online
                    if (player.isOnline()) {
                        player.sendMessage(this.plugin.getConfigController().getMessage("dialog-join-message"));
                    }
                });
            } else {
                // Run kick player logic on the main thread
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    // Check if player is still online
                    if (player.isOnline()) {
                        player.closeDialog();
                        player.kick(this.plugin.getConfigController().getMessage("dialog-join-disconnect"));
                    }
                });
            }

            this.awaitingResponse.remove(player.getUniqueId());
        });
    }
}