package net.cc.core.command.friend;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.util.Constants;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class FriendCommand {

    private final CorePlugin plugin;

    public FriendCommand(final CorePlugin plugin, final Commands registrar) {
        this.plugin = plugin;

        var node = Commands.literal("friend")
                .requires(stack -> stack.getSender().hasPermission(Constants.PERMISSION_COMMAND_FRIEND))
                .then(new FriendAddCommand(plugin).build())
                .then(new FriendRemoveCommand(plugin).build())
                .then(new FriendListCommand(plugin).build())
                .build();

        registrar.register(node, "Manage your friends", List.of("f"));
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        return Command.SINGLE_SUCCESS;
    }
}
