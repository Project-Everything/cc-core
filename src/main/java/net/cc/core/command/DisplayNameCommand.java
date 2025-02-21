package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.CoreUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public final class DisplayNameCommand {

    private final CorePlugin plugin;
    private final MiniMessage mm;

    // Constructor
    public DisplayNameCommand(final CorePlugin plugin, Commands commands) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();

        var node = Commands.literal("displayname")
                .requires(s -> s.getSender().hasPermission(CoreUtils.PERMISSION_COMMAND_NAME) && s.getSender() instanceof Player)
                .executes(this::view)
                .then(Commands.literal("reset")
                        .executes(this::reset))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(this::set))
                .build();

        commands.register(node, "Set your display name", List.of("name"));
    }

    // Method for executing /displayname
    private int view(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer weavePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (weavePlayer != null) {
            String displayName = weavePlayer.getDisplayName();
            player.sendMessage(mm.deserialize("<gold>Your display name is:</gold> " + displayName + "<reset><gold>.\nSet your display name with /displayname {name}.</gold>"));
        }
        return Command.SINGLE_SUCCESS;
    }

    // Method for executing /displayname {value}
    private int set(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (corePlayer != null) {
            String displayName = ctx.getArgument("name", String.class);
            Component displayNameComponent = mm.deserialize(displayName);
            String rawDisplayName = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);

            if (player.getName().equals(rawDisplayName)) {
                corePlayer.setDisplayName(displayName);
                player.sendMessage(mm.deserialize("<gold>Display name has been set to </gold>" + displayName + "<reset><gold>.</gold>"));
                plugin.getCorePlayerManager().updatePlayer(corePlayer);
            } else {
                player.sendMessage(mm.deserialize("<red>Display name does not match your player name</red>"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    // Method for executing /displayname reset
    private int reset(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (corePlayer != null) {
            String displayName = "<gray>" + player.getName() + "</gray>";
            corePlayer.setDisplayName(displayName);
            player.sendMessage(mm.deserialize("<gold>Display name has been reset.</gold>"));
            plugin.getCorePlayerManager().updatePlayer(corePlayer);
        }
        return Command.SINGLE_SUCCESS;
    }
}
