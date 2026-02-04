package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command class for the /displayname command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class DisplayNameCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("displayname")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_DISPLAYNAME.get()))
                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                        .executes(this::setDisplayName))
                .then(Commands.literal("reset")
                        .executes(this::resetDisplayName))
                .executes(this::viewDisplayName)
                .build();

        registrar.register(command, "Set your display name", List.of("name"));
    }

    // Sets the player's display name
    private int setDisplayName(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        final String rawDisplayName = context.getArgument("displayName", String.class);
        final Component displayName = this.plugin.getMiniMessage().deserialize(rawDisplayName);

        // Validate display name
        if (!validateDisplayName(player, displayName)) {
            player.sendMessage(this.plugin.getConfigController().getMessage("command-displayname-mismatch"));
            return 0;
        }

        // Set the display name
        corePlayer.setDisplayName(displayName);

        // Send the success message
        player.sendMessage(this.plugin.getConfigController().getMessage("command-displayname-set",
                Placeholder.component("display_name", displayName)));
        return Command.SINGLE_SUCCESS;
    }

    // Resets the player's display name
    private int resetDisplayName(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        final String rawDisplayName = "<gray>" + player.getName() + "</gray>";
        final Component displayName = this.plugin.getMiniMessage().deserialize(rawDisplayName);

        // Reset the display name
        corePlayer.setDisplayName(displayName);

        // Send the success message
        player.sendMessage(this.plugin.getConfigController().getMessage("command-displayname-reset",
                Placeholder.component("display_name", displayName)));
        return Command.SINGLE_SUCCESS;
    }

    // Views the player's display name
    private int viewDisplayName(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Send the success message
        player.sendMessage(this.plugin.getConfigController().getMessage("command-displayname",
                Placeholder.component("display_name", corePlayer.getDisplayName())));

        return Command.SINGLE_SUCCESS;
    }

    // Checks if a displayName component matches the player's name
    private boolean validateDisplayName(final Player player, final Component displayName) {
        final String rawDisplayName = PlainTextComponentSerializer.plainText().serialize(displayName);
        return player.getName().equals(rawDisplayName);
    }
}
