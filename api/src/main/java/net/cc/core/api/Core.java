package net.cc.core.api;

import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.api.model.CoreServer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API interface for cc-core.
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public interface Core {

    CoreServer getServer();

    CorePlayer getPlayer(final UUID uuid);

    CorePlayer getPlayer(final String name);

    Set<CorePlayer> getPlayers();

    Set<CorePlayer> getPlayers(final CoreServer server);

    CompletableFuture<CorePlayer> getPlayerAsync(final UUID uuid);

    CompletableFuture<CorePlayer> getPlayerAsync(final String name);

}
