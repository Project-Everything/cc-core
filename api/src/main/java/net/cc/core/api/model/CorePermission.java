package net.cc.core.api.model;

/**
 * Model enum for a permission.
 *
 * @since 1.0
 */
public enum CorePermission {
    CHAT_COLOR("cc.chat.color"),

    CHANNEL_GLOBAL("cc.channel.global"),
    CHANNEL_STAFF_CHAT("cc.channel.staffchat"),
    CHANNEL_MOD_CHAT("cc.channel.modchat"),
    CHANNEL_ADMIN_CHAT("cc.channel.adminchat"),

    CHANNEL_PLOTS_LOCAL("cc.channel.plots.local"),

    CHANNEL_EARTH_GLOBAL("cc.channel.earth.global"),
    CHANNEL_EARTH_LOCAL("cc.channel.earth.local"),
    CHANNEL_EARTH_TOWN_CHAT("cc.channel.earth.townchat"),
    CHANNEL_EARTH_NATION_CHAT("cc.channel.earth.nationchat"),

    COMMAND_BLOCK("cc.command.block"),
    COMMAND_BROADCAST("cc.command.broadcast"),
    COMMAND_CHAT_SPY("cc.command.chatspy"),
    COMMAND_CLEAR_CHAT("cc.command.clearchat"),
    COMMAND_COINS_OTHER("cc.command.coins.other"),
    COMMAND_COINS("cc.command.coins"),
    COMMAND_CORE("cc.command.core"),
    COMMAND_DISPLAYNAME("cc.command.displayname"),
    COMMAND_LEADERBOARD("cc.command.leaderboard"),
    COMMAND_LIST("cc.command.list"),
    COMMAND_MEOW("cc.command.meow"),
    COMMAND_MEOWS_OTHER("cc.command.meows.other"),
    COMMAND_MEOWS("cc.command.meows"),
    COMMAND_MESSAGE_COLOR("cc.command.message.color"),
    COMMAND_MESSAGE("cc.command.message"),
    COMMAND_NEAR("cc.command.near"),
    COMMAND_NICKNAME_COLOR("cc.command.nickname.color"),
    COMMAND_NICKNAME("cc.command.nickname"),
    COMMAND_REPLY("cc.command.reply"),
    COMMAND_SEEN("cc.command.seen"),
    COMMAND_STAFFLIST("cc.command.stafflist"),
    COMMAND_UNBLOCK("cc.command.unblock"),
    COMMAND_VANISH("cc.command.vanish"),
    COMMAND_VOTES_OTHER("cc.command.votes.other"),
    COMMAND_VOTES("cc.command.votes");

    final String permission;

    // Constructor
    CorePermission(final String permission) {
        this.permission = permission;
    }

    public String get() {
        return this.permission;
    }

}
