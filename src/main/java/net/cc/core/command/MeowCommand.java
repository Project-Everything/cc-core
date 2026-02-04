package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.CoreUtils;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.model.config.MeowConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.List;

// Class for the /meow command
@RequiredArgsConstructor
public final class MeowCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        final var command = Commands.literal("meow")
                .requires(s -> s.getSender().hasPermission(CorePermission.COMMAND_MEOW.get()))
                .executes(this::execute)
                .build();

        registrar.register(command, "meow", List.of("mew", "purr", "purreow"));
    }

    // Executes the command
    private int execute(final CommandContext<CommandSourceStack> context) {
        final Player player = CoreUtils.getPlayerSender(this.plugin, context.getSource());
        if (player == null) return 0;

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final MeowConfig meowConfig = this.plugin.getConfigController().getMeowConfig();

            // Check if player is on the blacklist
            if (meowConfig.getBlacklist().contains(player.getUniqueId().toString())) {
                player.sendMessage(this.plugin.getConfigController().getMessage("error-sender-blacklist"));
                return;
            }

            final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(player);
            final long cooldown = meowConfig.getCooldown();
            final long sentUnixTime = System.currentTimeMillis();

            // Constructs the redis key for player
            final String redisKey = Constants.REDIS_KEY_MEOW + ":" + player.getUniqueId();

            // Get the logged UNIX timestamp
            this.plugin.getRedisController().get(redisKey).thenAccept(loggedUnixTimeStr -> {
                // Check if the value does not exist
                if (loggedUnixTimeStr == null) {
                    corePlayer.setCoins(corePlayer.getCoins() + meowConfig.getDefaultReward());
                    this.plugin.getServer().getScheduler().runTask(this.plugin,
                            () -> player.getWorld().playSound(player, Sound.ENTITY_CAT_PURREOW, SoundCategory.PLAYERS, 50, 1));

                    // TODO add special reward chance (see commit 142427e)

                    // If cooldown is set to 0, the key will never expire
                    if (cooldown > 0) {
                        this.plugin.getRedisController().set(redisKey, String.valueOf(sentUnixTime), cooldown);
                    }

                    corePlayer.setMeows(corePlayer.getMeows() + 1);
                    player.sendMessage(this.plugin.getConfigController().getMessage("command-meow",
                            Placeholder.parsed("meows", String.valueOf(corePlayer.getMeows()))));

                    // Publish meow message
                    this.plugin.getRedisController().publish(Constants.REDIS_CHANNEL_MEOW, corePlayer.getUniqueId().toString());
                } else {
                    long remainingCooldown = (meowConfig.getCooldown() * 1000L) - (sentUnixTime - Long.parseLong(loggedUnixTimeStr));

                    if (remainingCooldown < 0) { // Failsafe! if the cooldown is negative, that means that the entry exists but should have expired
                        remainingCooldown = 0; // Avoid error in duration formating
                        this.plugin.getRedisController().delete(redisKey); // Manually delete the key
                    }

                    final String remainingCooldownDuration = DurationFormatUtils.formatDurationWords(remainingCooldown, true, true);
                    player.sendMessage(this.plugin.getConfigController().getMessage("error-command-cooldown",
                            Placeholder.parsed("remain_time", String.valueOf(remainingCooldownDuration))));
                }
            });
        });

        return Command.SINGLE_SUCCESS;
    }

}
