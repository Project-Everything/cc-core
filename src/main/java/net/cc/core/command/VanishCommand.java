package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.util.Constants;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class VanishCommand {

    private final CorePlugin plugin;
    private final MiniMessage mm;

    public VanishCommand(final CorePlugin plugin, Commands commands) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();

        LiteralCommandNode<CommandSourceStack> node = Commands.literal("vanish")
                .requires(stack -> stack.getSender().hasPermission(Constants.PERMISSION_COMMAND_VANISH))
                .executes(this::execute0)
                .build();

        commands.register(node, "Toggle vanish", List.of("v"));
    }

    private int execute0(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (sender instanceof Player player) {
            CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
            if (corePlayer != null) {
                boolean vanished = corePlayer.isVanished();
                if (vanished) {
                    player.sendMessage(mm.deserialize(Constants.MESSAGE_COMMAND_VANISH_DISABLE));
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                } else {
                    player.sendMessage(mm.deserialize(Constants.MESSAGE_COMMAND_VANISH_ENABLE));
                }
                corePlayer.setVanished(!vanished);
                plugin.getCorePlayerManager().updatePlayer(corePlayer);
                return Command.SINGLE_SUCCESS;
            }
        } else {
            sender.sendMessage(mm.deserialize(Constants.MESSAGE_SENDER_NOT_PLAYER));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}
