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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command class for the /meows command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class MeowsCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("meows")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_MEOWS.get()))
                .executes(this::meows)
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_MEOWS_OTHER.get()))
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::meowsOther))
                .build();

        registrar.register(command, "View your meows");
    }

    // Gets the meows of the sender
    private int meows(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        final int meows = corePlayer.getMeows();

        player.sendMessage(this.plugin.getConfigController().getMessage("command-meows",
                Placeholder.parsed("amount", String.valueOf(meows)))
        );

        return Command.SINGLE_SUCCESS;
    }

    // Gets the meows of a target player
    private int meowsOther(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(context.getArgument("player", String.class));

        // Check if player was found
        if (corePlayer != null) {
            final String username = corePlayer.getName();
            final int meows = corePlayer.getMeows();

            sender.sendMessage(this.plugin.getConfigController().getMessage("command-meows-other",
                    Placeholder.parsed("player", username),
                    Placeholder.parsed("amount", String.valueOf(meows)))
            );
        } else {
            // Player does not exist
            sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
        }
        return Command.SINGLE_SUCCESS;
    }

}
