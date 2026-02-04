package net.cc.core.api.model.player;

import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.CoreStanding;
import net.kyori.adventure.text.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * Model interface for a player.
 *
 * @since 1.0
 */
public interface CorePlayer {

    UUID getUniqueId();

    long getCreatedAt();

    long getUpdatedAt();

    void setUpdatedAt(final long updatedAt, final boolean silent);

    long getJoinedAt();

    void setJoinedAt(final long joinedAt);

    String getName();

    void setName(final String name);

    CoreServer getServer();

    void setServer(final CoreServer server);

    EnumMap<CoreServer, CoreChannel> getChannels();

    void setChannels(final EnumMap<CoreServer, CoreChannel> channels);

    CoreChannel getChannel(final CoreServer server);

    void setChannel(final CoreServer server, final CoreChannel channel);

    CoreStanding getStanding();

    void setStanding(final CoreStanding standing);

    UUID getRecent();

    void setRecent(final UUID recent);

    Component getDisplayName();

    void setDisplayName(final Component displayName);

    Component getNickname();

    void setNickname(final Component nickname);

    List<UUID> getFriends();

    void setFriends(final List<UUID> friends);

    void friend(final UUID uuid);

    void unfriend(final UUID uuid);

    List<UUID> getBlocked();

    void setBlocked(final List<UUID> blocked);

    void block(final UUID uuid);

    void unblock(final UUID uuid);

    boolean isOnline();

    void setOnline(final boolean online);

    boolean isVanished();

    void setVanished(final boolean vanished);

    boolean isSpying();

    void setSpying(final boolean spying);

    boolean isAllowTpa();

    void setAllowTpa(final boolean allowTpa);

    boolean isAllowMention();

    void setAllowMention(final boolean allowMention);

    boolean isConfirmed();

    void setConfirmed(final boolean confirmed, final boolean silent);

    int getCoins();

    void setCoins(final int coins);

    int getVotes();

    void setVotes(final int votes);

    int getMeows();

    void setMeows(final int meows);

    void save();
}
