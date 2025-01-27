package net.cc.core.task;

import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import org.bukkit.entity.Player;

public class UpdatePlayersTask implements Runnable {

    private final CorePlugin plugin;

    public UpdatePlayersTask(final CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            CorePlayer corePlayer = plugin.getCorePlayerManager().getPlayer(player);
            if (corePlayer != null) {
                plugin.getDatabaseManager().saveCorePlayer(corePlayer);
            }
        }
    }
}
