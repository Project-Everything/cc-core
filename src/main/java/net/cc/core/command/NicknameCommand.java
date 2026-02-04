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
 * Command class for the /nickname command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class NicknameCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("nickname")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_NICKNAME.get()))
                .then(Commands.argument("nickname", StringArgumentType.greedyString())
                        .executes(this::setNickname))
                .then(Commands.literal("reset")
                        .executes(this::resetNickname))
                .executes(this::viewNickname)
                .build();

        registrar.register(command, "Set your nickname", List.of("rpname"));
    }

    // Sets the player's nickname
    private int setNickname(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Create nickname component
        final String rawNickname = context.getArgument("nickname", String.class);
        final boolean color = this.plugin.getServiceController().hasPermission(corePlayer.getUniqueId(),
                CorePermission.COMMAND_NICKNAME_COLOR.get());

        final Component nickname = color
                ? this.plugin.getMiniMessage().deserialize(rawNickname)
                : PlainTextComponentSerializer.plainText().deserialize(rawNickname);

        // Set the nickname
        corePlayer.setNickname(nickname);

        player.sendMessage(this.plugin.getConfigController().getMessage("command-nickname-set",
                Placeholder.component("nickname", corePlayer.getNickname())));
        return Command.SINGLE_SUCCESS;
    }

    // Resets the player's nickname
    private int resetNickname(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        // Reset the display name
        corePlayer.setNickname(Component.empty());

        player.sendMessage(this.plugin.getConfigController().getMessage("command-nickname-reset"));
        return Command.SINGLE_SUCCESS;
    }

    // Views the player's nickname
    private int viewNickname(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        final CorePlayer corePlayer = CoreUtils.getCorePlayer(this.plugin, player);
        if (corePlayer == null) return 0;

        final String rawNickname = this.plugin.getMiniMessage().serialize(corePlayer.getNickname());

        if (!(rawNickname.isEmpty() || rawNickname.equals("<!italic></!italic>"))) {
            player.sendMessage(this.plugin.getConfigController().getMessage("command-nickname",
                    Placeholder.component("nickname", corePlayer.getNickname())));
        } else {
            player.sendMessage(this.plugin.getConfigController().getMessage("command-nickname-empty"));
        }

        return Command.SINGLE_SUCCESS;
    }
}
