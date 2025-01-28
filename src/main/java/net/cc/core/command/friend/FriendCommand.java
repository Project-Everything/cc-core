package net.cc.core.command.friend;

import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.util.CoreUtils;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class FriendCommand {

    public FriendCommand(final CorePlugin plugin, final Commands registrar) {
        var node = Commands.literal("friend")
                .requires(stack -> stack.getSender().hasPermission(CoreUtils.PERMISSION_COMMAND_FRIEND))
                .then(new FriendAddCommand(plugin).build())
                .then(new FriendRemoveCommand(plugin).build())
                .then(new FriendListCommand(plugin).build())
                .build();

        registrar.register(node, "Manage your friends", List.of("f"));
    }
}
