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
 * Command class for the /votes command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class VotesCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("votes")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_VOTES.get()))
                .executes(this::votes)
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_VOTES_OTHER.get()))
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::votesOther))
                .build();

        registrar.register(command, "View your tokens");
    }

    // Gets the votes of the sender
    private int votes(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        final int votes = corePlayer.getVotes();

        player.sendMessage(this.plugin.getConfigController().getMessage("command-votes",
                Placeholder.parsed("amount", String.valueOf(votes)))
        );

        return Command.SINGLE_SUCCESS;
    }

    // Gets the votes of a target player
    private int votesOther(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(context.getArgument("player", String.class));

        // Check if player was found
        if (corePlayer != null) {
            final String username = corePlayer.getName();
            final int votes = corePlayer.getVotes();

            sender.sendMessage(this.plugin.getConfigController().getMessage("command-votes-other",
                    Placeholder.parsed("player", username),
                    Placeholder.parsed("amount", String.valueOf(votes)))
            );
        } else {
            // Player does not exist
            sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
        }
        return Command.SINGLE_SUCCESS;
    }

}
