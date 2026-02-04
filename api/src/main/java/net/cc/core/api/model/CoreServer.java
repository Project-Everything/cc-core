package net.cc.core.api.model;

/**
 * Model enum for a server.
 *
 * @since 1.0
 */
public enum CoreServer {
    DEFAULT(CoreChannel.GLOBAL),
    LOBBY(CoreChannel.GLOBAL),
    PLOTS(CoreChannel.GLOBAL),
    EARTH(CoreChannel.EARTH_GLOBAL_CHAT),
    ADVENTURE(CoreChannel.GLOBAL),
    ISLANDS(CoreChannel.GLOBAL),
    LEGACY(CoreChannel.GLOBAL),
    BUILD(CoreChannel.GLOBAL);

    private final CoreChannel defaultChannel;

    // Constructor
    CoreServer(final CoreChannel defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public CoreChannel getDefaultChannel() {
        return this.defaultChannel;
    }
}
