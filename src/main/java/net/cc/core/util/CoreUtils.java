package net.cc.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class CoreUtils {

    /* Permission Nodes */
    public static final String PERMISSION_COMMAND_FRIEND = "cc.command.friend";
    public static final String PERMISSION_COMMAND_MEOW = "cc.command.meow";
    public static final String PERMISSION_COMMAND_NAME = "cc.command.name";
    public static final String PERMISSION_COMMAND_NICKNAME = "cc.command.nickname";

    /* Messages */
    public static Component getSenderNotPlayerComponent() {
        return Component.text("Only players may use this command.", NamedTextColor.RED);
    }
}
