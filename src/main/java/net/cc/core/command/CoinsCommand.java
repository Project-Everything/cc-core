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
 * Command class for the /coins command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CoinsCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("coins")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_COINS.get()))
                .executes(this::coins)
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_COINS_OTHER.get()))
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::coinsOther))
                .build();

        registrar.register(command, "View your coins");
    }

    // Gets the coins of the sender
    private int coins(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        final int coins = corePlayer.getCoins();

        player.sendMessage(this.plugin.getConfigController().getMessage("command-coins",
                Placeholder.parsed("amount", String.valueOf(coins))));

        return Command.SINGLE_SUCCESS;
    }

    // Gets the coins of a target player
    private int coinsOther(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(context.getArgument("player", String.class));

        // Check if player was found
        if (corePlayer != null) {
            final String username = corePlayer.getName();
            final int coins = corePlayer.getCoins();

            sender.sendMessage(this.plugin.getConfigController().getMessage("command-coins-other",
                    Placeholder.parsed("player", username),
                    Placeholder.parsed("amount", String.valueOf(coins))));
        } else {
            // Player does not exist
            sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
        }
        return Command.SINGLE_SUCCESS;
    }

}
