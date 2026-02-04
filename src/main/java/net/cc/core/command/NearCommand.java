package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Command class for the /near command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class NearCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("near")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_NEAR.get()))
                .executes(this::near)
                .build();

        registrar.register(command, "View nearby players");
    }

    // Sends a list of nearby players to a player
    private int near(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final List<Component> components = new ArrayList<>();
        final NumberFormat df = new DecimalFormat("0.00");

        // Iterate through all online players to add their display names to the list
        for (final Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
            // Filter out players in other worlds
            if (player.getWorld() != onlinePlayer.getWorld()) continue;

            final double distance = player.getLocation().distance(onlinePlayer.getLocation());

            // Filter out players that aren't nearby
            if (player.getUniqueId().equals(onlinePlayer.getUniqueId())) continue;
            if (distance > 500) continue;

            // Get the CorePlayer instance for the online player
            final CorePlayer onlineCorePlayer = this.plugin.getPlayerController().getPlayer(onlinePlayer);
            if (onlineCorePlayer == null) continue;

            // Filter out vanished players
            if (!(player.hasPermission(CorePermission.COMMAND_VANISH.get())) && onlineCorePlayer.isVanished()) continue;

            components.add(this.plugin.getConfigController().getMessage("command-near-player",
                    Placeholder.component("display_name", onlineCorePlayer.getDisplayName()),
                    Placeholder.parsed("distance", df.format(distance))));
        }

        // Send message to the player
        if (!components.isEmpty()) {
            player.sendMessage(this.plugin.getConfigController().getMessage("command-near",
                    Placeholder.component("list", Component.join(JoinConfiguration.commas(true), components))));
        } else {
            player.sendMessage(this.plugin.getConfigController().getMessage("command-near-empty"));
        }

        return Command.SINGLE_SUCCESS;
    }
}
