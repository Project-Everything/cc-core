package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.util.Constants;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class NicknameCommand {

    private final CorePlugin plugin;
    private final MiniMessage mm;

    public NicknameCommand(final CorePlugin plugin, Commands commands) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();

        LiteralCommandNode<CommandSourceStack> node = Commands.literal("nickname")
                .requires(s -> s.getSender().hasPermission(Constants.PERMISSION_COMMAND_NICKNAME) && s.getSender() instanceof Player)
                .executes(this::view)
                .then(Commands.literal("reset")
                        .executes(this::reset))
                .then(Commands.argument("nickname", StringArgumentType.greedyString())
                        .executes(this::set))
                .build();

        commands.register(node, "Set your nickname", List.of("rpname"));
    }

    private int view(final CommandContext<CommandSourceStack> ctx) {
        final Player player = (Player) ctx.getSource().getSender();
        final CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (corePlayer != null) {
            final String nickname = corePlayer.getNickname();
            if (!nickname.isEmpty()) {
                player.sendMessage(mm.deserialize(Constants.MESSAGE_COMMAND_NICKNAME_VIEW, Placeholder.parsed("nickname", nickname)));
            } else {
                player.sendMessage(mm.deserialize(Constants.MESSAGE_COMMAND_NICKNAME_EMPTY));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int set(final CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
        if (corePlayer != null) {
            String nickname = ctx.getArgument("nickname", String.class);
            corePlayer.setNickname(nickname);
            player.sendMessage(mm.deserialize(Constants.MESSAGE_COMMAND_NICKNAME_SET, Placeholder.parsed("nickname", nickname)));
            plugin.getCorePlayerManager().updatePlayer(corePlayer);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int reset(final CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
        if (corePlayer != null) {
            corePlayer.setNickname("");
            player.sendMessage(mm.deserialize(Constants.MESSAGE_COMMAND_NICKNAME_RESET));
            plugin.getCorePlayerManager().updatePlayer(corePlayer);
        }
        return Command.SINGLE_SUCCESS;
    }
}
