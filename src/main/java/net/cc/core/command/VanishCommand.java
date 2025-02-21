package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.CoreUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class VanishCommand {

    private final CorePlugin plugin;
    private final MiniMessage mm;

    // Constructor
    public VanishCommand(final CorePlugin plugin, Commands commands) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();

        var node = Commands.literal("vanish")
                .requires(stack -> stack.getSender().hasPermission(CoreUtils.PERMISSION_COMMAND_VANISH))
                .executes(this::execute0)
                .build();
        commands.register(node, "Toggle vanish", List.of("v"));
    }

    // Method for executing the command
    private int execute0(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (sender instanceof Player player) {
            CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
            if (corePlayer != null) {
                boolean vanished = corePlayer.isVanished();
                if (vanished) {
                    player.sendMessage(mm.deserialize("<gold>Vanish has been</gold> <red>disabled</red><gold>.</gold>"));
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                } else {
                    player.sendMessage(mm.deserialize("<gold>Vanish has been</gold> <green>enabled</green><gold>.</gold>"));
                }
                corePlayer.setVanished(!vanished);
                plugin.getCorePlayerManager().updatePlayer(corePlayer);
                return Command.SINGLE_SUCCESS;
            }
        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}
