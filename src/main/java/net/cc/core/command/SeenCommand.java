package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.command.suggestion.CorePlayerSuggestionProvider;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.command.CommandSender;

/**
 * Command class for the /seen command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class SeenCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("seen")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_SEEN.get()))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::seen))
                .build();

        registrar.register(command, "See when a player was last online");
    }

    // Sends a last-seen message to a player
    private int seen(final CommandContext<CommandSourceStack> context) {
        final String targetName = context.getArgument("player", String.class);
        final CommandSender sender = context.getSource().getSender();
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(targetName);

        if (corePlayer != null) {
            // Player exists
            if (corePlayer.isOnline()) {
                // Player is online
                return this.showOnlineMessage(sender, corePlayer);
            } else {
                // Player is offline
                return this.showOfflineMessage(sender, corePlayer);
            }
        } else {
            // No player found
            this.plugin.getConfigController().getMessage("error-player-not-found");
            return 0;
        }
    }

    // Seen message for an online player
    private int showOnlineMessage(final CommandSender sender, final CorePlayer corePlayer) {
        final long duration = System.currentTimeMillis() - corePlayer.getJoinedAt();
        final String durationString = DurationFormatUtils.formatDurationWords(duration, true, true);

        sender.sendMessage(this.plugin.getConfigController().getMessage("command-seen-online",
                Placeholder.component("player", corePlayer.getDisplayName()),
                Placeholder.parsed("time", durationString)));
        return Command.SINGLE_SUCCESS;
    }

    // Seen message for an offline player
    private int showOfflineMessage(final CommandSender sender, final CorePlayer corePlayer) {
        final long duration = System.currentTimeMillis() - corePlayer.getUpdatedAt();
        final String durationString = DurationFormatUtils.formatDurationWords(duration, true, true);

        sender.sendMessage(this.plugin.getConfigController().getMessage("command-seen-offline",
                Placeholder.component("player", corePlayer.getDisplayName()),
                Placeholder.parsed("time", durationString)));
        return Command.SINGLE_SUCCESS;
    }
}
