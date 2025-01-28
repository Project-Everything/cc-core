package net.cc.core.command.friend;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.command.argument.CorePlayerArgumentType;
import net.cc.core.player.CorePlayer;
import net.cc.core.player.CorePlayerManager;
import net.cc.core.util.CoreUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings({"UnstableApiUsage"})
public final class FriendAddCommand {

    private final CorePlugin plugin;
    private final CorePlayerManager playerManager;

    public FriendAddCommand(final CorePlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getCorePlayerManager();
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("add")
                .executes(context -> {
                    final CommandSender sender = context.getSource().getSender();
                    sender.sendMessage(Component.text("/" + context.getInput() + " <player>", NamedTextColor.RED));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("player", new CorePlayerArgumentType(plugin))
                        .executes(this::execute));
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            final CorePlayer source = playerManager.getPlayer(player);
            final CorePlayer target = context.getArgument("player", CorePlayer.class);

            source.addFriend(target.getMojangId());
            playerManager.updatePlayer(source);
            player.sendMessage(Component.text("Added " + target.getUsername() + " as a friend!", NamedTextColor.GOLD));

            final Player targetPlayer = plugin.getServer().getPlayer(target.getMojangId());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(Component.text(source.getUsername() + " added you as a friend!", NamedTextColor.GOLD));
            }
        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
        }
        return Command.SINGLE_SUCCESS;
    }
}
