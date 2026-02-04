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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command class for the /fake-quit command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class FakeQuitCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("fake-quit")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_VANISH.get()))
                .executes(this::execute)
                .build();

        registrar.register(command, "Create a fake quit message", List.of("fake-leave"));
    }

    // Executes the command
    private int execute(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        this.plugin.getPlayerController().vanishPlayer(corePlayer, player, false);

        // Send fake message
        final Component joinMessage = this.plugin.getConfigController().getMessage("server-quit-message",
                Placeholder.component("player", corePlayer.getDisplayName()));
        this.plugin.getServer().broadcast(joinMessage);

        return Command.SINGLE_SUCCESS;
    }
}
