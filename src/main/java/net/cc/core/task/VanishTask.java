package net.cc.core.task;

import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task class for player vanish.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class VanishTask extends BukkitRunnable {

    private final CorePlugin plugin;

    @Override
    public void run() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (!(player.isOnline())) continue;

            final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);
            if (corePlayer != null) {
                // Disable vanish if permission is missing
                if (!player.hasPermission(CorePermission.COMMAND_VANISH.get())) {
                    corePlayer.setVanished(false);
                }

                // Add invisibility to vanished player
                if (corePlayer.isVanished()) {
                    player.setCollidable(false);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5, 1, false, true));
                }

                // Toggle visibility of player
                for (final Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
                    if (corePlayer.isVanished() && !onlinePlayer.hasPermission(CorePermission.COMMAND_VANISH.get())) {
                        onlinePlayer.hidePlayer(this.plugin, player);
                    } else {
                        onlinePlayer.showPlayer(this.plugin, player);
                    }
                }

                // Disable entity targeting
                if (corePlayer.isVanished()) {
                    for (final Entity entity : player.getWorld().getEntities() ) {
                        if (entity instanceof Creature creature) {
                            if (creature.getTarget() != null && creature.getTarget().equals(player)) {
                                creature.setTarget(null);
                            }
                        }
                    }
                }
            } else {
                this.plugin.getComponentLogger().error("VanishTask failed for '{}'", player.getName());
            }
        }
    }

}
