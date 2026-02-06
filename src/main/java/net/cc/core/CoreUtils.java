package net.cc.core;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.cc.core.api.model.CoreAlpha;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;
import net.cc.core.api.model.player.CorePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for plugin-wide utility methods.
 *
 * @since 1.0.0
 */
public final class CoreUtils {

    // Gets a Player sender
    public static Player getPlayerSender(final CorePlugin plugin, final CommandSourceStack source) {
        final CommandSender sender = source.getSender();

        // Verify the sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigController().getMessage("error-sender-not-player"));
            return null;
        }
        return player;
    }

    // Gets a core player
    public static CorePlayer getCorePlayer(final CorePlugin plugin, final Player player) {
        final CorePlayer corePlayer = plugin.getPlayerController().getPlayer(player);

        // Check if corePlayer exists
        if (corePlayer == null) {
            player.sendMessage(plugin.getConfigController().getMessage("error-core-player-invalid"));
            return null;
        }
        return corePlayer;
    }

    // Creates an Enum map from a comma-delimited string
    public static EnumMap<CoreServer, CoreChannel> stringToEnumMap(final String string) {
        if (string == null || string.isEmpty()) {
            return new EnumMap<>(CoreServer.class);
        }

        return new EnumMap<>(Arrays.stream(string.split(","))
                .map(String::trim)
                .map(s -> s.split(":", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> CoreServer.valueOf(parts[0].toUpperCase()),
                        parts -> CoreChannel.valueOf(parts[1].toUpperCase()),
                        (existing, replacement) -> replacement,
                        () -> new EnumMap<>(CoreServer.class)
                )));
    }

    // Creates a comma-delimited string from an Enum map
    public static String enumMapToString(final EnumMap<CoreServer, CoreChannel> enumMap) {
        return enumMap.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    // Creates a comma-delimited string from a UUID list
    public static String uuidListToString(final List<UUID> list) {
        return list.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    // Creates a UUID list from a comma-delimited string
    public static List<UUID> stringToUuidList(final String string) {
        return new ArrayList<>(Arrays.stream(string.split(","))
                .map(String::trim)
                .filter(s -> !(s.isEmpty()))
                .map(UUID::fromString)
                .toList());
    }

    // Creates a comma-delimited string from an Enum set
    public static String enumSetToString(final Set<CoreAlpha> alphas) {
        return alphas.stream()
                .map(a -> a.toString().toUpperCase())
                .collect(Collectors.joining(","));
    }

    // Create an Enum set from a comma-delimited string
    public static Set<CoreAlpha> stringToEnumSet(final String string) {
        if (string == null || string.isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(string.split(","))
                .map(String::trim)
                .map(s -> CoreAlpha.valueOf(s.toUpperCase())).collect(Collectors.toSet());
    }

}
