package net.cc.core.task;

import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.model.player.PaperCorePlayer;
import net.cc.core.model.player.PaperCorePlayerMemento;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Task class for queueing player data.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class PlayerQueueTask extends BukkitRunnable {

    private final CorePlugin plugin;
    private final Set<UUID> queue = new HashSet<>();

    // Add a player to the save queue
    public void queue(final PaperCorePlayer corePlayer) {
        this.plugin.getPlayerController().syncPlayer(corePlayer);
        this.queue.add(corePlayer.getUniqueId());

        if (this.plugin.isDebugMode()) {
            this.plugin.getComponentLogger().warn(
                    "Added player '{}:{}' to the queue",
                    corePlayer.getUniqueId(), corePlayer.getName()
            );
        }
    }

    @Override
    public void run() {
        final List<PaperCorePlayerMemento> mementos = new ArrayList<>();

        final List<UUID> uuids = this.plugin.getPlayerController().getPlayers().stream()
                .map(CorePlayer::getUniqueId)
                .toList();

        // Create snapshot of all queued players
        for (final UUID uuid : uuids) {
            if (this.queue.contains(uuid)) {
                final PaperCorePlayer current = (PaperCorePlayer) this.plugin.getPlayerController().getPlayer(uuid);
                mementos.add(current.createMemento(this.plugin));
            }
        }

        // Clear the queue
        this.queue.clear();

        // Save players to database
        for (final PaperCorePlayerMemento memento : mementos) {
            this.plugin.getDataController().savePlayer(memento.toCorePlayer(this.plugin));

            if (this.plugin.isDebugMode()) {
                this.plugin.getComponentLogger().warn(
                        "Saved player '{}:{}' to the database",
                        memento.uniqueId(), memento.username()
                );
            }
        }
    }

}