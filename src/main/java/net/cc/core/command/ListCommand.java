package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for the /list command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ListCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("list")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_LIST.get()))
                .executes(this::execute)
                .build();

        registrar.register(command, "List online players");
    }

    // Executes the command
    private int execute(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final List<Component> components = new ArrayList<>();

        // Iterate through all CorePlayer instances and add their display names to the list
        this.plugin.getPlayerController().getPlayers().forEach(corePlayer -> {
            if (!(corePlayer.isOnline())) return;
            if (corePlayer.isVanished() && !(sender.hasPermission(CorePermission.COMMAND_VANISH.get()))) return;

            final Component component = this.plugin.getPlayerController().getPlayerComponent(corePlayer);
            components.add(component);
        });

        // Send message to the player
        if (!(components.isEmpty())) {
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-list",
                    Placeholder.component("list", Component.join(JoinConfiguration.commas(true), components))));
        } else {
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-list-empty"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
