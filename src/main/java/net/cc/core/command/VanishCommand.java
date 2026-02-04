package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command class for the /vanish command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class VanishCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("vanish")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_VANISH.get()))
                .executes(this::vanish)
                .build();

        registrar.register(command, "Toggle vanish", List.of("v"));
    }

    // Toggles vanish for a player
    private int vanish(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        this.plugin.getPlayerController().vanishPlayer(corePlayer, player);
        return Command.SINGLE_SUCCESS;
    }
}
