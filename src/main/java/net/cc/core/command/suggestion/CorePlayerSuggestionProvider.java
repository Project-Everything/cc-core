package net.cc.core.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Suggestion provider for CorePlayer instances.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CorePlayerSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    private final CorePlugin plugin;
    private final boolean showSelf;
    private final boolean requireOnline;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        this.plugin.getPlayerController().getPlayers().stream()
                .filter(corePlayer -> corePlayer.getName().toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(corePlayer -> {
                    final CommandSender sender = context.getSource().getSender();
                    final boolean canSeeVanished = !(sender.hasPermission(CorePermission.COMMAND_VANISH.get()))
                            && corePlayer.isVanished();

                    if (requireOnline) {
                        // Hide vanished players from online list
                        if (corePlayer.isVanished() && !(canSeeVanished)) return;
                    }

                    // Hide offline players
                    if (this.requireOnline && !(corePlayer.isOnline())) return;

                    if ((sender instanceof Player player) && !(this.showSelf)) {
                        // Prevent player from being in list
                        final CorePlayer playerCorePlayer = this.plugin.getPlayerController().getPlayer(player);
                        if (corePlayer.getUniqueId().equals(playerCorePlayer.getUniqueId())) return;
                    }

                    builder.suggest(corePlayer.getName());
                });

        return builder.buildFuture();
    }
}
