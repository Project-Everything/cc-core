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
 * Command class for the /block command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class BlockCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("block")
                .requires(s -> s.getSender().hasPermission(CorePermission.COMMAND_BLOCK.get()))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, false, false))
                        .executes(this::execute))
                .build();

        registrar.register(command, "Block a player", List.of("ignore"));
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
            // Prevent adding self
            player.sendMessage(this.plugin.getConfigController().getMessage("command-block-self"));
            return 0;
        }

        final CorePlayer targetCorePlayer = this.plugin.getPlayerController().getPlayer(targetName);

        if (targetCorePlayer != null) {
            if (corePlayer.getBlocked().contains(targetCorePlayer.getUniqueId())) {
                // Player is already blocked
                player.sendMessage(this.plugin.getConfigController().getMessage("command-block-invalid",
                        Placeholder.parsed("username", targetName)));
                return 0;
            }

            corePlayer.block(targetCorePlayer.getUniqueId());
            player.sendMessage(this.plugin.getConfigController().getMessage("command-block",
                    Placeholder.parsed("username", targetName)));
        } else {
            player.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }
}
