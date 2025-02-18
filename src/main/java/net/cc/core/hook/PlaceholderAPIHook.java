package net.cc.core.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderAPIHook extends PlaceholderExpansion {

    private final CorePlugin plugin;

    // Constructor
    public PlaceholderAPIHook(final CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cc-core";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SpektrSoyuz";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (corePlayer != null) {
            switch (params) {
                case "displayname" -> {
                    return corePlayer.getDisplayName();
                }
                case "nickname" -> {
                    return corePlayer.getNickname();
                }
                case "nickname_full" -> {
                    String nickname = corePlayer.getNickname();
                    if (!nickname.isEmpty()) {
                        return "<yellow>(" + corePlayer.getNickname() + "<reset><yellow>)</yellow> ";
                    }
                    return "";
                }
            }
        }
        return null;
    }
}
