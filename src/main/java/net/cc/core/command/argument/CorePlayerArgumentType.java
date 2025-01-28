package net.cc.core.command.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.cc.core.CorePlugin;
import net.cc.core.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
@SuppressWarnings({"UnstableApiUsage"})
public class CorePlayerArgumentType implements CustomArgumentType.Converted<CorePlayer, String> {

    private final CorePlugin plugin;

    public CorePlayerArgumentType(final CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CorePlayer convert(String nativeType) throws CommandSyntaxException {
        final CorePlayer current = plugin.getCorePlayerManager().getPlayer(nativeType);

        if (current != null) {
            return current;
        }
        Message message = MessageComponentSerializer.message().serialize(Component.text("Player not found.", NamedTextColor.RED));
        throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (final CorePlayer corePlayer : plugin.getCorePlayerManager().getPlayers()) {
            if (corePlayer.getUsername().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(corePlayer.getUsername());
            }
        }
        return builder.buildFuture();
    }
}
