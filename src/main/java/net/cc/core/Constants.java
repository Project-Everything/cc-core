package net.cc.core;

import io.papermc.paper.util.Tick;
import net.kyori.adventure.key.Key;

import java.time.Duration;

/**
 * Class for plugin-wide constants.
 *
 * @since 1.0.0
 */
public final class Constants {

    // Config paths
    public static final String CONFIG_PRIMARY = "config.conf";
    public static final String CONFIG_MESSAGES = "messages.conf";

    // Redis channels
    public static final String REDIS_CHANNEL_SYNC = "core:player:sync";
    public static final String REDIS_CHANNEL_UPDATE = "core:player:update";
    public static final String REDIS_CHANNEL_BROADCAST = "core:broadcast";
    public static final String REDIS_CHANNEL_VANISH = "core:vanish";
    public static final String REDIS_CHANNEL_MEOW = "core:meow";
    public static final String REDIS_CHANNEL_CHAT_MESSAGE = "core:chat_message";
    public static final String REDIS_CHANNEL_PRIVATE_MESSAGE = "core:private_message";
    public static final String REDIS_CHANNEL_MAIL_MESSAGE = "core:mail_message";

    // Redis keys
    public static final String REDIS_KEY_MEOW = "core:meows";

    // Keys
    public static final Key KEY_DIALOG_JOIN_ACCEPT = Key.key("core:join_dialog/accept");
    public static final Key KEY_DIALOG_JOIN_DECLINE = Key.key("core:join_dialog/decline");

    // Ticks
    public static final long _30S = Tick.tick().fromDuration(Duration.ofSeconds(30));
    public static final long _1S = Tick.tick().fromDuration(Duration.ofSeconds(1));
    public static final long _05S = Tick.tick().fromDuration(Duration.ofMillis(500));

    // Misc
    public static final int LOCAL_CHAT_RADIUS = 100;

}
