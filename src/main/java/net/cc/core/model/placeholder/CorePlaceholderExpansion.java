package net.cc.core.model.placeholder;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Model class for a PlaceholderAPI expansion
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CorePlaceholderExpansion extends PlaceholderExpansion {

    private final CorePlugin plugin;

    // Expansion ID
    @Override
    public @NotNull String getIdentifier() {
        return "ae-core";
    }

    // Author
    @Override
    public @NotNull String getAuthor() {
        return "SpektrSoyuz";
    }

    // Version
    @Override
    public @NotNull String getVersion() {
        return "2.0";
    }

    // Handles placeholders
    @Override
    public String onPlaceholderRequest(final Player player, final @NotNull String params) {
        switch (params) {
            // Player placeholder
            case "player_tab" -> {
                final Component component = this.plugin.getPlayerController().getPlayerTabComponent(player);
                return MiniMessage.miniMessage().serialize(component);
            }
            case "player" -> {
                final Component component = this.plugin.getPlayerController().getPlayerComponent(player);
                return MiniMessage.miniMessage().serialize(component);
            }
            // Coins placeholder
            case "coins" -> {
                final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

                return corePlayer != null
                        ? String.valueOf(corePlayer.getCoins())
                        : "?";
            }
            // Votes placeholder
            case "votes" -> {
                final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

                return corePlayer != null
                        ? String.valueOf(corePlayer.getVotes())
                        : "?";
            }
            // Meows placeholder
            case "meows" -> {
                final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

                return corePlayer != null
                        ? String.valueOf(corePlayer.getMeows())
                        : "?";
            }
            // Online player count
            case "online" -> {
                return String.valueOf(this.plugin.getPlayerController().getPlayerCount(player));
            }
        }
        // No placeholder found
        return null;
    }

}