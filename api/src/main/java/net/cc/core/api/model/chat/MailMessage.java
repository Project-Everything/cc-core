package net.cc.core.api.model.chat;

import net.cc.core.api.model.CoreServer;

import java.util.UUID;

/**
 * Model record for a mail message.
 *
 * @since 1.0
 */
public record MailMessage(
        UUID sender,
        UUID target,
        CoreServer server,
        long timestamp,
        String message,
        String formattedMessage
) {

}
