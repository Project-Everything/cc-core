package net.cc.core.listener;

import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.player.CorePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Listener class for Entity events.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class EntityListener implements Listener {

    private final CorePlugin plugin;

    // Registers the listener
    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    // Handle entity targeting
    @EventHandler
    public void onEntityTarget(final EntityTargetEvent event) {
        // Check if target is a player
        if (event.getTarget() instanceof Player player) {
            final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

            if (corePlayer != null) {
                // Check if core player is vanished
                if (corePlayer.isVanished()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // Handles item pickup
    @EventHandler
    public void onItemPickup(final EntityPickupItemEvent event) {
        final Entity entity = event.getEntity();

        // Check if entity is a player
        if (entity instanceof Player player) {
            final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);

            if (corePlayer != null) {
                // Check if core player is vanished
                if (corePlayer.isVanished()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}