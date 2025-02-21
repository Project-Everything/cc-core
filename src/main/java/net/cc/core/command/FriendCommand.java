package net.cc.core.command;

import com.mojang.brigadier.Command;
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
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"UnstableApiUsage"})
public final class FriendCommand {

    private final CorePlugin plugin;
    private final CorePlayerManager playerManager;

    // Constructor
    public FriendCommand(final CorePlugin plugin, final Commands registrar) {
        this.plugin = plugin;
        this.playerManager = plugin.getCorePlayerManager();

        var node = Commands.literal("friend")
                .requires(stack -> stack.getSender().hasPermission(CoreUtils.PERMISSION_COMMAND_FRIEND))
                .then(Commands.literal("add")
                        .executes(context -> {
                            final CommandSender sender = context.getSource().getSender();
                            sender.sendMessage(Component.text("/" + context.getInput() + " <player>", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("player", new CorePlayerArgumentType(plugin))
                                .executes(this::add)))
                .then(Commands.literal("remove")
                        .executes(context -> {
                            final CommandSender sender = context.getSource().getSender();
                            sender.sendMessage(Component.text("/" + context.getInput() + " <player>", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("player", new CorePlayerArgumentType(plugin))
                                .executes(this::remove)))
                .then(Commands.literal("list")
                        .executes(this::list))
                .build();

        registrar.register(node, "Manage your friends", List.of("f"));
    }

    // Method for executing /friend add
    public int add(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            final CorePlayer source = playerManager.getPlayer(player);
            final CorePlayer target = context.getArgument("player", CorePlayer.class);

            if (source.equals(target)) {
                player.sendMessage(Component.text("You cannot add yourself as a friend.", NamedTextColor.RED));
            } else if (source.isFriend(target.getMojangId())) {
                player.sendMessage(Component.text(target.getUsername() + " is already a friend.", NamedTextColor.RED));
            } else {
                source.addFriend(target.getMojangId());
                playerManager.updatePlayer(source);
                player.sendMessage(Component.text("Added " + target.getUsername() + " as a friend!", NamedTextColor.GOLD));

                final Player targetPlayer = plugin.getServer().getPlayer(target.getMojangId());
                if (targetPlayer != null) {

                    targetPlayer.sendMessage(Component.text(source.getUsername() + " added you as a friend!", NamedTextColor.GOLD));
                }
            }
        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
        }
        return Command.SINGLE_SUCCESS;
    }

    // Method for executing /friend remove
    public int remove(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            final CorePlayer source = playerManager.getPlayer(player);
            final CorePlayer target = context.getArgument("player", CorePlayer.class);

            if (!(source.isFriend(target.getMojangId()))) {
                player.sendMessage(Component.text(target.getUsername() + " is not a friend.", NamedTextColor.RED));
            } else {
                source.removeFriend(target.getMojangId());
                playerManager.updatePlayer(source);
                player.sendMessage(Component.text("Removed " + target.getUsername() + " from your friends list.", NamedTextColor.GOLD));
            }
        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
        }
        return Command.SINGLE_SUCCESS;
    }

    // Method for executing /friend list
    public int list(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            final CorePlayer source = playerManager.getPlayer(player);
            final List<String> friends = source.getFriends();
            final List<String> usernames = new ArrayList<>();

            if (!friends.isEmpty()) {
                for (final String friendString : friends) {
                    final UUID mojangId = UUID.fromString(friendString);
                    final CorePlayer friend = playerManager.getPlayer(mojangId);
                    if (friend != null) {
                        usernames.add(friend.getUsername());
                    }
                }
            }

            final int count = usernames.size();
            final String list = StringUtils.join(usernames, ", ");
            final Component component = Component.text("Friends [" + count + "]: " + list, NamedTextColor.GOLD);
            player.sendMessage(component);
        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
        }
        return Command.SINGLE_SUCCESS;
    }
}
