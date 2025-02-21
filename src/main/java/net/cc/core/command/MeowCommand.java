package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayerManager;
import net.cc.core.util.CoreUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class MeowCommand {

    private final CorePlugin plugin;
    private final CorePlayerManager playerManager;

    // Constructor
    public MeowCommand(final CorePlugin plugin, final Commands registrar) {
        this.plugin = plugin;
        this.playerManager = plugin.getCorePlayerManager();

        var node = Commands.literal("meow")
                .requires(stack -> stack.getSender().hasPermission(CoreUtils.PERMISSION_COMMAND_MEOW))
                .executes(this::execute)
                .build();

        registrar.register(node, "Meow!", List.of("mew", "purr", "purreow"));
    }

    // Method to execute command
    public int execute(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            String input = context.getInput();
            Sound sound;
            Component message;

            switch (input) {
                case "mew" -> {
                    message = Component.text("Mew!", NamedTextColor.LIGHT_PURPLE);
                    sound = Sound.ENTITY_CAT_BEG_FOR_FOOD;
                }
                case "purr" -> {
                    message = Component.text("Purr!", NamedTextColor.DARK_PURPLE);
                    sound = Sound.ENTITY_CAT_PURR;
                }
                case "purreow" -> {
                    message = Component.text("Purreow!", NamedTextColor.RED);
                    sound = Sound.ENTITY_CAT_PURREOW;
                }
                default -> {
                    message = Component.text("Meow!", NamedTextColor.GREEN);
                    sound = Sound.ENTITY_CHICKEN_AMBIENT;
                }
            }
            player.sendMessage(message);
            player.playSound(player.getLocation(), sound, 10, 1);
        } else {
            sender.sendMessage(CoreUtils.getSenderNotPlayerComponent());
        }
        return Command.SINGLE_SUCCESS;
    }
}
