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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command class for the /reply command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class ReplyCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("reply")
                .requires(s -> s.getSender().hasPermission(CorePermission.COMMAND_REPLY.get()))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(this::execute))
                .build();

        registrar.register(command, "Reply to a player", List.of("r"));
    }

    // Executes the command
    public int execute(final CommandContext<CommandSourceStack> ctx) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, ctx.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Check if recent contact exists
        if (corePlayer.getRecent().equals(player.getUniqueId())) {
            player.sendMessage(this.plugin.getConfigController().getMessage("error-recent-contact-empty"));
            return 0;
        }

        final CorePlayer targetCorePlayer = this.plugin.getPlayerController().getPlayer(corePlayer.getRecent());

        if (targetCorePlayer != null) {
            if (targetCorePlayer.isOnline()) {
                // Handle private message
                final String message = ctx.getArgument("message", String.class);
                this.plugin.getChatController().sendPrivateMessage(corePlayer, targetCorePlayer, message);

                player.sendMessage(this.plugin.getConfigController().getMessage("command-message-send",
                        Placeholder.parsed("username", targetCorePlayer.getName()),
                        Placeholder.parsed("message", message)
                ));
                return Command.SINGLE_SUCCESS;
            } else {
                // Recent contact is offline
                player.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-online",
                        Placeholder.parsed("username", targetCorePlayer.getName())));
                return 0;
            }
        } else {
            player.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            return 0;
        }
    }

}
