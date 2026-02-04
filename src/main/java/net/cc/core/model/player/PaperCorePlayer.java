package net.cc.core.model.player;

import lombok.Getter;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.CoreStanding;
import net.cc.core.api.model.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Model class for a player.
 *
 * @since 1.0.0
 */
@Getter
public final class PaperCorePlayer implements CorePlayer {

    private final UUID uniqueId;
    private final long createdAt;
    private long updatedAt;
    private long joinedAt;
    private String name;
    private CoreServer server;
    private EnumMap<CoreServer, CoreChannel> channels;
    private CoreStanding standing;
    private UUID recent;
    private Component displayName;
    private Component nickname;
    private List<UUID> friends;
    private List<UUID> blocked;
    private boolean online;
    private boolean vanished;
    private boolean spying;
    private boolean allowTpa;
    private boolean allowMention;
    private boolean confirmed;
    private int coins;
    private int votes;
    private int meows;
    private final Consumer<PaperCorePlayer> saveConsumer;

    // Constructor
    public PaperCorePlayer(
            final CorePlugin plugin,
            final Player player
    ) {
        this.uniqueId = player.getUniqueId();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.joinedAt = System.currentTimeMillis();
        this.name = player.getName();
        this.server = plugin.getCoreServer();
        this.channels = this.createNewChannelMap();
        this.standing = CoreStanding.EXCELLENT;
        this.recent = player.getUniqueId();
        this.displayName = Component.text(player.getName(), NamedTextColor.GRAY);
        this.nickname = Component.empty();
        this.friends = new ArrayList<>();
        this.blocked = new ArrayList<>();
        this.online = true;
        this.vanished = false;
        this.spying = false;
        this.allowTpa = true;
        this.allowMention = true;
        this.confirmed = false;
        this.coins = 0;
        this.votes = 0;
        this.meows = 0;
        this.saveConsumer = plugin.getPlayerQueueTask()::queue;
    }

    // Constructor
    public PaperCorePlayer(
            final CorePlugin plugin,
            final UUID uniqueId,
            final long createdAt,
            final long updatedAt,
            final long joinedAt,
            final String name,
            final CoreServer server,
            final EnumMap<CoreServer, CoreChannel> channels,
            final CoreStanding standing,
            final UUID recent,
            final Component displayName,
            final Component nickname,
            final List<UUID> friends,
            final List<UUID> blocked,
            final boolean online,
            final boolean vanished,
            final boolean spying,
            final boolean allowTpa,
            final boolean allowMention,
            final boolean confirmed,
            final int coins,
            final int votes,
            final int meows
    ) {
        this.uniqueId = uniqueId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.joinedAt = joinedAt;
        this.name = name;
        this.server = server;
        this.channels = channels;
        this.standing = standing;
        this.recent = recent;
        this.displayName = displayName;
        this.nickname = nickname;
        this.friends = friends;
        this.blocked = blocked;
        this.online = online;
        this.vanished = vanished;
        this.spying = spying;
        this.allowTpa = allowTpa;
        this.allowMention = allowMention;
        this.confirmed = confirmed;
        this.coins = coins;
        this.votes = votes;
        this.meows = meows;
        this.saveConsumer = plugin.getPlayerQueueTask()::queue;
    }

    // Sets the player's updated at value
    @Override
    public void setUpdatedAt(final long updatedAt, final boolean silent) {
        if (!(Objects.equals(this.updatedAt, updatedAt))) {
            this.updatedAt = updatedAt;

            if (!(silent)) {
                this.save();
            }
        }
    }

    // Sets the player's joined at value
    @Override
    public void setJoinedAt(final long joinedAt) {
        if (!(Objects.equals(this.joinedAt, joinedAt))) {
            this.joinedAt = joinedAt;
            this.save();
        }
    }

    // Sets the player's username
    @Override
    public void setName(final String name) {
        if (!(Objects.equals(this.name, name))) {
            this.name = name;
            this.save();
        }
    }

    // Sets the player's current server
    @Override
    public void setServer(final CoreServer server) {
        if (!(Objects.equals(this.server, server))) {
            this.server = server;
            this.save();
        }
    }

    @Override
    public void setChannels(final EnumMap<CoreServer, CoreChannel> channels) {
        if (!(Objects.equals(this.channels, channels))) {
            this.channels = channels;
            this.save();
        }
    }

    // Gets the player's chat channel on a server
    @Override
    public CoreChannel getChannel(final CoreServer server) {
        return this.channels.get(server);
    }

    // Sets the player's chat channel on a server
    @Override
    public void setChannel(final CoreServer server, final CoreChannel channel) {
        if (!(this.channels.get(server) == channel)) {
            this.channels.put(server, channel);
            this.save();
        }
    }

    // Sets the player's standing
    @Override
    public void setStanding(final CoreStanding standing) {
        if (!(this.standing == standing)) {
            this.standing = standing;
            this.save();
        }
    }

    // Sets the player's most recent contact
    @Override
    public void setRecent(final UUID recent) {
        if (!(Objects.equals(this.recent, recent))) {
            this.recent = recent;
            this.save();
        }
    }

    // Gets the player's display name
    @Override
    public Component getDisplayName() {
        return this.displayName.decorationIfAbsent(
                TextDecoration.ITALIC,
                TextDecoration.State.FALSE
        );
    }

    // Sets the player's display name
    @Override
    public void setDisplayName(final Component displayName) {
        if (!(Objects.equals(this.displayName, displayName))) {
            this.displayName = displayName;
            this.save();
        }
    }

    // Gets the player's nickname
    @Override
    public Component getNickname() {
        return this.nickname.decorationIfAbsent(
                TextDecoration.ITALIC,
                TextDecoration.State.FALSE
        );
    }

    // Sets the player's nickname
    @Override
    public void setNickname(final Component nickname) {
        if (!(Objects.equals(this.nickname, nickname))) {
            this.nickname = nickname;
            this.save();
        }
    }

    @Override
    public void setFriends(final List<UUID> friends) {
        if (!(Objects.equals(this.friends, friends))) {
            this.friends = friends;
            this.save();
        }
    }

    @Override
    public void friend(final UUID uuid) {
        if (!(this.friends.contains(uuid))) {
            friends.add(uuid);
            this.save();
        }
    }

    @Override
    public void unfriend(final UUID uuid) {
        if (this.friends.contains(uuid)) {
            friends.remove(uuid);
            this.save();
        }
    }

    @Override
    public void setBlocked(final List<UUID> blocked) {
        if (!(Objects.equals(this.blocked, blocked))) {
            this.blocked = blocked;
            this.save();
        }
    }

    @Override
    public void block(final UUID uuid) {
        if (!(this.blocked.contains(uuid))) {
            blocked.add(uuid);
            this.save();
        }
    }

    @Override
    public void unblock(final UUID uuid) {
        if (this.blocked.contains(uuid)) {
            blocked.remove(uuid);
            this.save();
        }
    }

    // Sets the player's online state
    @Override
    public void setOnline(final boolean online, final boolean silent) {
        if (!(Objects.equals(this.online, online))) {
            this.online = online;

            if (!(silent)) {
                this.save();
            }
        }
    }

    // Sets the player's vanish state
    @Override
    public void setVanished(final boolean vanished) {
        if (!(Objects.equals(this.vanished, vanished))) {
            this.vanished = vanished;
            this.save();
        }
    }

    // Sets the player's chat spy state
    @Override
    public void setSpying(final boolean spying) {
        if (!(Objects.equals(this.spying, spying))) {
            this.spying = spying;
            this.save();
        }
    }

    // Sets the player's allow-tpa state
    @Override
    public void setAllowTpa(final boolean allowTpa) {
        if (!(Objects.equals(this.allowTpa, allowTpa))) {
            this.allowTpa = allowTpa;
            this.save();
        }
    }

    // Sets the player's allow-mention state
    @Override
    public void setAllowMention(final boolean allowMention) {
        if (!(Objects.equals(this.allowMention, allowMention))) {
            this.allowMention = allowMention;
            this.save();
        }
    }

    // Sets the player's confirmed state
    @Override
    public void setConfirmed(final boolean confirmed, final boolean silent) {
        if (!(Objects.equals(this.confirmed, confirmed))) {
            this.confirmed = confirmed;
            if (!(silent)) {
                this.save();
            }
        }
    }

    // Sets the player's coins value
    @Override
    public void setCoins(final int coins) {
        if (!(Objects.equals(this.coins, coins))) {
            this.coins = coins;
            this.save();
        }
    }

    // Sets the player's votes value
    @Override
    public void setVotes(final int votes) {
        if (!(Objects.equals(this.votes, votes))) {
            this.votes = votes;
            this.save();
        }
    }

    // Sets the player's meows value
    @Override
    public void setMeows(final int meows) {
        if (!(Objects.equals(this.meows, meows))) {
            this.meows = meows;
            this.save();
        }
    }

    // Saves the player state
    @Override
    public void save() {
        this.saveConsumer.accept(this);
    }

    // Creates a default channel map for the player
    public EnumMap<CoreServer, CoreChannel> createNewChannelMap() {
        return Arrays.stream(CoreServer.values())
                .collect(Collectors.toMap(
                        server -> server,
                        CoreServer::getDefaultChannel,
                        (existing, replacement) -> existing,
                        () -> new EnumMap<>(CoreServer.class)
                ));
    }

    // Creates a Memento pattern for this object
    public PaperCorePlayerMemento createMemento(final CorePlugin plugin) {
        final String server = this.server.toString();
        final String channels = CoreUtils.enumMapToString(this.channels);
        final String standing = this.standing.toString();
        final String displayName = plugin.getMiniMessage().serialize(this.displayName);
        final String nickname = plugin.getMiniMessage().serialize(this.nickname);
        final String friends = CoreUtils.uuidListToString(this.friends);
        final String blocked = CoreUtils.uuidListToString(this.blocked);

        return new PaperCorePlayerMemento(
                this.uniqueId,
                this.createdAt,
                this.updatedAt,
                this.joinedAt,
                this.name,
                server,
                channels,
                standing,
                this.recent,
                displayName,
                nickname,
                friends,
                blocked,
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
