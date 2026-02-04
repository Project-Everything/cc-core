package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command class for the /clearchat command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ClearChatCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("clearchat")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_CLEAR_CHAT.get()))
                .executes(this::execute)
                .build();

        registrar.register(command, "Clear the chat");
    }

    // Executes the command
    private int execute(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        final Component message;

        if (sender instanceof Player player) {
            // Player is executing the command
            message = this.plugin.getConfigController().getMessage(
                    "command-clear-chat",
                    Placeholder.parsed("username", player.getName())
            );
        } else {
            // Console is executing the command
            message = this.plugin.getConfigController().getMessage(
                    "command-clear-chat-console"
            );
        }

        final String string = "\n".repeat(500);
        final Component lines = this.plugin.getMiniMessage().deserialize(string);

        this.plugin.getComponentLogger().info(message);
        this.plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
            // Send message to audience
            onlinePlayer.sendMessage(lines);
            onlinePlayer.sendMessage(message);
        });

        return Command.SINGLE_SUCCESS;
    }

}
