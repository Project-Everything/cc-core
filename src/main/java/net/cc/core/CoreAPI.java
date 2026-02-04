package net.cc.core;

import lombok.RequiredArgsConstructor;
import net.cc.core.api.Core;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * API implementation for ae-core.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CoreAPI implements Core {

    private final CorePlugin plugin;

    @Override
    public @NotNull CoreServer getServer() {
        return this.plugin.getCoreServer();
    }

    @Override
    public CorePlayer getPlayer(final UUID uuid) {
        return this.plugin.getPlayerController().getPlayer(uuid);
    }

    @Override
    public CorePlayer getPlayer(final String name) {
        return this.plugin.getPlayerController().getPlayer(name);
    }

    @Override
    public Set<CorePlayer> getPlayers() {
        return new HashSet<>(this.plugin.getPlayerController().getPlayers());
    }

    @Override
    public Set<CorePlayer> getPlayers(final CoreServer server) {
        return this.plugin.getPlayerController().getPlayers().stream()
                .filter(corePlayer -> corePlayer.getServer() == server)
                .collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<CorePlayer> getPlayerAsync(final UUID uuid) {
        return this.plugin.getPlayerController().getPlayerAsync(uuid);
    }

    @Override
    public @NotNull CompletableFuture<CorePlayer> getPlayerAsync(final String name) {
        return this.plugin.getPlayerController().getPlayerAsync(name);
    }
}
