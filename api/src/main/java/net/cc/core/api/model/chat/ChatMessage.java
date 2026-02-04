package net.cc.core.api.model.chat;

import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;

import java.util.UUID;

/**
 * Model record for a chat message.
 *
 * @since 1.0
 */
public record ChatMessage(
        UUID sender,
        CoreChannel channel,
        CoreServer server,
        long timestamp,
        String message,
        String formattedMessage
) {

}
