package net.cc.core.task;

import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.cc.core.util.CoreUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class UpdateVanishTask implements Runnable {

    private final CorePlugin plugin;

    // Constructor
    public UpdateVanishTask(final CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isOnline()) continue;

            CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
            if (corePlayer == null) {
                plugin.getLogger().warning("UpdateVanishTask failed for " + player.getName());
                continue;
            }

            if (!player.hasPermission(CoreUtils.PERMISSION_COMMAND_VANISH)) {
                corePlayer.setVanished(false);
            }

            if (corePlayer.isVanished()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            }

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (corePlayer.isVanished()) {
                    onlinePlayer.hidePlayer(plugin, player);
                } else {
                    onlinePlayer.showPlayer(plugin, player);
                }
            }
        }
    }
}
