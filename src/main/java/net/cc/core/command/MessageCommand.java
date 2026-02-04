package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.command.suggestion.CorePlayerSuggestionProvider;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command class for the /message command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class MessageCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("message")
                .requires(s -> s.getSender().hasPermission(CorePermission.COMMAND_MESSAGE.get()))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, false, true))
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(this::execute)))
                .build();

        registrar.register(command, "Message a player", List.of("msg", "dm", "whisper", "w"));
    }

    // Executes the command
    public int execute(final CommandContext<CommandSourceStack> ctx) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, ctx.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Resolve player
        final String targetName = ctx.getArgument("player", String.class);

        if (player.getName().equals(targetName)) {
            // Prevent sending message to self
            player.sendMessage(this.plugin.getConfigController().getMessage("command-message-self"));
            return 0;
        }

        final CorePlayer targetCorePlayer = this.plugin.getPlayerController().getPlayer(targetName);

        if (targetCorePlayer != null) {
            if (!(targetCorePlayer.isOnline())) {
                // Player is offline
                player.sendMessage(this.plugin.getConfigController().getMessage(
                        "error-player-not-online",
                        Placeholder.parsed("username", targetName)
                ));
                return 0;
            }

            // Handle private message
            final String message = ctx.getArgument("message", String.class);
            this.plugin.getChatController().sendPrivateMessage(corePlayer, targetCorePlayer, message);

            player.sendMessage(this.plugin.getConfigController().getMessage(
                    "command-message-send",
                    Placeholder.parsed("username", targetName),
                    Placeholder.parsed("message", message)
            ));
            corePlayer.setRecent(targetCorePlayer.getUniqueId());
            return Command.SINGLE_SUCCESS;
        } else {
            player.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            return 0;
        }
    }

}
