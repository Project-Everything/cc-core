package net.cc.core.task;

import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.player.CorePlayer;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task class for cross-server player synchronization.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class PlayerSyncTask extends BukkitRunnable {

    private final CorePlugin plugin;

    @Override
    public void run() {
        // Update state of all online players
        this.plugin.getServer().getOnlinePlayers().forEach(player -> {
            final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);
            if (corePlayer != null) {
                // Update player silently
                corePlayer.setUpdatedAt(System.currentTimeMillis(), true);
                corePlayer.setOnline(true, true);

                this.plugin.getPlayerController().validateChannels(corePlayer);
                this.plugin.getPlayerController().updatePlayer(corePlayer);
            }
        });

        // Set player to offline after 1 second
        this.plugin.getPlayerController().getPlayers().forEach(corePlayer -> {
            if (corePlayer.isOnline() && System.currentTimeMillis() - corePlayer.getUpdatedAt() > 1000) {
                corePlayer.setOnline(false, false);

                if (this.plugin.isDebugMode()) {
                    this.plugin.getComponentLogger().warn(
                            "Set '{}:{}' to offline",
                            corePlayer.getUniqueId(), corePlayer.getName()
                    );
                }
            }
        });
    }

}
