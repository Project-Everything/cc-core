package net.cc.core.command.friend;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;

@SuppressWarnings({"UnstableApiUsage"})
public final class FriendAddCommand {

    private final CorePlugin plugin;

    public FriendAddCommand(final CorePlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("add")
                .executes(this::execute);
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        return Command.SINGLE_SUCCESS;
    }
}
