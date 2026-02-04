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
 * Command class for the /chatspy command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ChatSpyCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("chatspy")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_CHAT_SPY.get()))
                .executes(this::execute)
                .build();

        registrar.register(command, "Toggle chat spy", List.of("spy"));
    }

    // Executes the command
    private int execute(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        if (corePlayer.isSpying()) {
            // Disable chat spy
            corePlayer.setSpying(false);
            player.sendMessage(this.plugin.getConfigController().getMessage("command-chat-spy-disable"));
        } else {
            // Enable chat spy
            corePlayer.setSpying(true);
            player.sendMessage(this.plugin.getConfigController().getMessage("command-chat-spy-enable"));
        }

        return Command.SINGLE_SUCCESS;
    }
}
