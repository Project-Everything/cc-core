package net.cc.core.model.player;

import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.CoreStanding;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * Memento pattern for a CorePlayer.
 *
 * @since 1.0.0
 */
public record PaperCorePlayerMemento(
        UUID uniqueId,
        long createdAt,
        long updatedAt,
        long joinedAt,
        String username,
        String server,
        String channels,
        String standing,
        UUID recent,
        String displayName,
        String nickname,
        String friends,
        String blocked,
        boolean online,
        boolean vanished,
        boolean spying,
        boolean allowTpa,
        boolean allowMention,
        boolean confirmed,
        int coins,
        int votes,
        int meows
) {
    // Creates a CorePlayer object from a Memento pattern
    public CorePlayer toCorePlayer(final CorePlugin plugin) {
        final CoreServer newServer = CoreServer.valueOf(this.server.toUpperCase());
        final EnumMap<CoreServer, CoreChannel> newChannels = CoreUtils.stringToEnumMap(this.channels);
        final CoreStanding newStanding = CoreStanding.valueOf(this.standing.toUpperCase());
        final Component newDisplayName = plugin.getMiniMessage().deserialize(this.displayName);
        final Component newNickname = plugin.getMiniMessage().deserialize(this.nickname);
        final List<UUID> newFriends = CoreUtils.stringToUuidList(this.friends);
        final List<UUID> newBlocked = CoreUtils.stringToUuidList(this.blocked);

        return new PaperCorePlayer(
                plugin,
                this.uniqueId,
                this.createdAt,
                this.updatedAt,
                this.joinedAt,
                this.username,
                newServer,
                newChannels,
                newStanding,
                this.recent,
                newDisplayName,
                newNickname,
                newFriends,
                newBlocked,
                this.online,
                this.vanished,
                this.spying,
                this.allowTpa,
                this.allowMention,
                this.confirmed,
                this.coins,
                this.votes,
                this.meows
        );
    }

}
