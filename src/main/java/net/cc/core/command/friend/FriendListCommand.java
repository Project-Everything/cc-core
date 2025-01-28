package net.cc.core.command.friend;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.player.CorePlayerManager;
import net.cc.core.util.CoreUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"UnstableApiUsage"})
public final class FriendListCommand {

    private final CorePlayerManager playerManager;

    public FriendListCommand(final CorePlugin plugin) {
        this.playerManager = plugin.getCorePlayerManager();
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("list")
                .executes(this::execute);
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            final CorePlayer source = playerManager.getPlayer(player);
            final List<CorePlayer> friends = new ArrayList<>();

            for (final UUID mojangId : source.getFriends()) {
                final CorePlayer friend = playerManager.getPlayer(mojangId);
                if (friend != null) {
                    friends.add(friend);
                }

                final int count = source.getFriends().size();
                final String list = friends.stream().toString();
                final Component component = Component.text("Friends [" + count + "]: " + list, NamedTextColor.GOLD);
                player.sendMessage(component);
            }

        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
        }
        return Command.SINGLE_SUCCESS;
    }
}
