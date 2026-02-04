package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for the /leaderboard command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class LeaderboardCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("leaderboard")
                .requires(s -> s.getSender().hasPermission(CorePermission.COMMAND_LEADERBOARD.get()))
                .then(Commands.literal("coins")
                        .executes(this::executeCoins))
                .then(Commands.literal("votes")
                        .executes(this::executeVotes))
                .then(Commands.literal("meows")
                        .executes(this::executeMeows))
                .build();

        registrar.register(command, "View the leaderboard", List.of("lb"));
    }

    // Executes the 'coins' sub-command
    private int executeCoins(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        final List<CorePlayer> corePlayers = this.plugin.getPlayerController().getTopCoins();

        // Create message
        final List<Component> components = new ArrayList<>();
        components.add(this.plugin.getConfigController().getMessage("command-leaderboard-coins-header"));

        for (final CorePlayer corePlayer : corePlayers) {
            components.add(this.plugin.getConfigController().getMessage("command-leaderboard-coins-body",
                    Placeholder.component("player", corePlayer.getDisplayName()),
                    Placeholder.parsed("value", String.valueOf(corePlayer.getCoins())))
            );
        }

        components.add(this.plugin.getConfigController().getMessage("command-leaderboard-coins-footer"));

        // Send message to sender
        final Component message = Component.join(JoinConfiguration.newlines(), components);
        sender.sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    // Executes the 'votes' sub-command
    private int executeVotes(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        final List<CorePlayer> corePlayers = this.plugin.getPlayerController().getTopVotes();

        // Create message
        final List<Component> components = new ArrayList<>();
        components.add(this.plugin.getConfigController().getMessage("command-leaderboard-votes-header"));

        for (final CorePlayer corePlayer : corePlayers) {
            components.add(this.plugin.getConfigController().getMessage("command-leaderboard-votes-body",
                    Placeholder.component("player", corePlayer.getDisplayName()),
                    Placeholder.parsed("value", String.valueOf(corePlayer.getVotes())))
            );
        }

        components.add(this.plugin.getConfigController().getMessage("command-leaderboard-votes-footer"));

        // Send message to sender
        final Component message = Component.join(JoinConfiguration.newlines(), components);
        sender.sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    // Executes the 'meows' sub-command
    private int executeMeows(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        final List<CorePlayer> corePlayers = this.plugin.getPlayerController().getTopMeows();

        // Create message
        final List<Component> components = new ArrayList<>();
        components.add(this.plugin.getConfigController().getMessage("command-leaderboard-meows-header"));

        for (final CorePlayer corePlayer : corePlayers) {
            components.add(this.plugin.getConfigController().getMessage("command-leaderboard-meows-body",
                    Placeholder.component("player", corePlayer.getDisplayName()),
                    Placeholder.parsed("value", String.valueOf(corePlayer.getMeows())))
            );
        }

        components.add(this.plugin.getConfigController().getMessage("command-leaderboard-meows-footer"));

        // Send message to sender
        final Component message = Component.join(JoinConfiguration.newlines(), components);
        sender.sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

}
