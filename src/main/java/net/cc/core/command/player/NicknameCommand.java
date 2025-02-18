package net.cc.core.command.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.util.CoreUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class NicknameCommand {

    private final CorePlugin plugin;
    private final MiniMessage mm;

    // Constructor
    public NicknameCommand(final CorePlugin plugin, Commands commands) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();

        var node = Commands.literal("nickname")
                .requires(s -> s.getSender().hasPermission(CoreUtils.PERMISSION_COMMAND_NICKNAME) && s.getSender() instanceof Player)
                .executes(this::view)
                .then(Commands.literal("reset")
                        .executes(this::reset))
                .then(Commands.argument("nickname", StringArgumentType.greedyString())
                        .executes(this::set))
                .build();

        commands.register(node, "Set your nickname", List.of("rpname"));
    }

    // Method for executing /nickname
    private int view(final CommandContext<CommandSourceStack> ctx) {
        final Player player = (Player) ctx.getSource().getSender();
        final CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (corePlayer != null) {
            final String nickname = corePlayer.getNickname();
            if (!nickname.isEmpty()) {
                player.sendMessage(mm.deserialize("<gold>Your current nickname is: </gold>" + nickname + "<reset><gold>.\nSet your nickname with /nickname <nickname>.</gold>"));
            } else {
                player.sendMessage(mm.deserialize("<gold>You do not have a nickname.\nSet your nickname with /nickname <nickname>.</gold>"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    // Method for executing /nickname {value}
    private int set(final CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
        if (corePlayer != null) {
            String nickname = ctx.getArgument("nickname", String.class);
            corePlayer.setNickname(nickname);
            player.sendMessage(mm.deserialize("<gold>Nickname has been set to </gold>" + nickname + "<reset><gold>.</gold>"));
            plugin.getCorePlayerManager().updatePlayer(corePlayer);
        }
        return Command.SINGLE_SUCCESS;
    }

    // Method for executing /nickname reset
    private int reset(final CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
        if (corePlayer != null) {
            corePlayer.setNickname("");
            player.sendMessage(mm.deserialize("<gold>Nickname has been reset.</gold>"));
            plugin.getCorePlayerManager().updatePlayer(corePlayer);
        }
        return Command.SINGLE_SUCCESS;
    }
}
