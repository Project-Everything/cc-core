package net.cc.core.listener;

import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final CorePlugin plugin;

    // Constructor
    public PlayerListener(final CorePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.joinMessage(null);

        final Player player = event.getPlayer();
        final CorePlayer corePlayer = plugin.getCorePlayerManager().loadPlayer(player);

        if (!corePlayer.isVanished()) {
            final Component component = Component.text("++ " + player.getName(), NamedTextColor.GREEN);
            plugin.getServer().broadcast(component);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        event.quitMessage(null);

        final Player player = event.getPlayer();
        final CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);

        if (!corePlayer.isVanished()) {
            final Component component = Component.text("-- " + player.getName(), NamedTextColor.RED);
            plugin.getServer().broadcast(component);
        }

        plugin.getDatabaseManager().saveCorePlayer(corePlayer);
    }
}
