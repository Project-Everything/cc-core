package net.cc.core.api.model;

import java.util.List;

/**
 * Model enum for a chat channel.
 *
 * @since 1.0
 */
public enum CoreChannel {
    // General channels
    GLOBAL(
            "global",
            "global",
            List.of("g"),
            CorePermission.CHANNEL_GLOBAL,
            true
    ),
    STAFF_CHAT(
            "staffchat",
            "staffchat",
            List.of("sc"),
            CorePermission.CHANNEL_STAFF_CHAT,
            true
    ),
    HELPER_CHAT(
            "helperchat",
            "helperchat",
            List.of("hc"),
            CorePermission.CHANNEL_HELPER_CHAT,
            true
    ),
    MOD_CHAT(
            "modchat",
            "modchat",
            List.of("mc"),
            CorePermission.CHANNEL_MOD_CHAT,
            true
    ),
    ADMIN_CHAT(
            "adminchat",
            "adminchat",
            List.of("ac"),
            CorePermission.CHANNEL_ADMIN_CHAT,
            true
    ),

    // Plots channels
    PLOTS_LOCAL_CHAT(
            "plots-local",
            "local",
            List.of("local", "l"),
            CorePermission.CHANNEL_PLOTS_LOCAL,
            false
    ),

    // Earth channels
    EARTH_GLOBAL_CHAT(
            "earth-global",
            "global",
            List.of("g"),
            CorePermission.CHANNEL_EARTH_GLOBAL,
            true
    ),
    EARTH_LOCAL_CHAT(
            "earth-local",
            "local",
            List.of("l"),
            CorePermission.CHANNEL_EARTH_LOCAL,
            false
    ),
    EARTH_TOWN_CHAT(
            "earth-townchat",
            "townchat",
            List.of("tc"),
            CorePermission.CHANNEL_EARTH_TOWN_CHAT,
            false
    ),
    EARTH_NATION_CHAT(
            "earth-nationchat",
            "nationchat",
            List.of("nc"),
            CorePermission.CHANNEL_EARTH_NATION_CHAT,
            false
    );

    private final String key;
    private final String command;
    private final List<String> aliases;
    private final CorePermission permission;
    private final boolean crossServer;

    // Constructor
    CoreChannel(
            final String key,
            final String command,
            final List<String> aliases,
            final CorePermission permission,
            final boolean crossServer
    ) {
        this.key = key;
        this.command = command;
        this.aliases = aliases;
        this.permission = permission;
        this.crossServer = crossServer;
    }

    public String getKey() {
        return this.key;
    }

    public String getCommand() {
        return this.command;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public CorePermission getPermission() {
        return this.permission;
    }

    public boolean isCrossServer() {
        return this.crossServer;
    }
}
