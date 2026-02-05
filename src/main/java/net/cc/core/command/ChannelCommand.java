package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

/**
 * Command class for a custom channel command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ChannelCommand {

    private final CorePlugin plugin;
    private final CoreChannel channel;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal(this.channel.getCommand())
                .requires(stack -> stack.getSender().hasPermission(this.channel.getPermission().get()))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(this::executeArgs))
                .executes(this::execute)
                .build();

        final String description = "Chat in the " + this.channel.getCommand() + " channel";
        registrar.register(command, description, this.channel.getAliases());
    }

    // Executes the command
    public int execute(final CommandContext<CommandSourceStack> ctx) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, ctx.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Check if player is already in channel
        if (corePlayer.getChannel(this.plugin.getCoreServer()) == this.channel) {
            player.sendMessage(this.plugin.getConfigController().getMessage("command-channel-current",
                    Placeholder.parsed("channel", this.channel.getCommand())));
            return 0;
        }

        // Set channel
        corePlayer.setChannel(this.plugin.getCoreServer(), this.channel);
        player.sendMessage(this.plugin.getConfigController().getMessage("command-channel",
                Placeholder.parsed("channel", this.channel.getCommand())));

        return Command.SINGLE_SUCCESS;
    }

    // Executes the command with arguments
    public int executeArgs(final CommandContext<CommandSourceStack> ctx) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, ctx.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Handle chat message
        final String message = ctx.getArgument("message", String.class);
        this.plugin.getChatController().sendChatMessage(corePlayer, this.channel, message);

        return Command.SINGLE_SUCCESS;
    }

}
