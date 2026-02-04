package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

/**
 * Command class for the /broadcast command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class BroadcastCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("broadcast")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_BROADCAST.get()))
                .then(Commands.literal("-g")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(this::executeGlobal)))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(this::execute))
                .build();

        registrar.register(command, "Broadcast a message", List.of("announce", "bc", "say"));
    }

    // Executes the command
    private int execute(final CommandContext<CommandSourceStack> context) {
        final String message = context.getArgument("message", String.class);

        final TagResolver resolver = Placeholder.parsed("message", message);
        this.plugin.getServer().forEachAudience(audience ->
                audience.sendMessage(this.plugin.getConfigController().getMessage("command-broadcast", resolver)));

        return Command.SINGLE_SUCCESS;
    }

    // Executes the command globally
    private int executeGlobal(final CommandContext<CommandSourceStack> context) {
        final String message = context.getArgument("message", String.class);

        this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_BROADCAST, message);
        return Command.SINGLE_SUCCESS;
    }
}
