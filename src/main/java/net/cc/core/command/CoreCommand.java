package net.cc.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.cc.core.CorePlugin;
import net.cc.core.api.model.CorePermission;
import net.cc.core.api.model.CoreStanding;
import net.cc.core.api.model.player.CorePlayer;
import net.cc.core.command.suggestion.CorePlayerSuggestionProvider;
import net.cc.core.model.player.PaperCorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Command class for the /core command.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class CoreCommand {

    private final CorePlugin plugin;

    // Registers the command
    public void register(final Commands registrar) {
        // /core debug
        final var debugLiteral = Commands.literal("debug")
                .executes(this::debug);

        // /core redis
        final var redisLiteral = Commands.literal("redis")
                .then(Commands.literal("get")
                        .then(Commands.argument("key", StringArgumentType.greedyString())
                                .executes(this::redisGet)))
                .then(Commands.literal("search")
                        .then(Commands.argument("key", StringArgumentType.greedyString())
                                .executes(this::redisSearch)));

        // /core reload
        final var reloadLiteral = Commands.literal("reload")
                .executes(this::reload);

        // /core reset
        final var resetLiteral = Commands.literal("reset")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::reset));

        // /core save
        final var saveLiteral = Commands.literal("save")
                .then(Commands.literal("all")
                        .executes(this::saveAll))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::save));

        // /core set allow-tpa
        final var setAllowTpa = Commands.literal("allow-tpa")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(this::setAllowTpa)));

        // /core set display-name
        final var setDisplayName = Commands.literal("display-name")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("value", StringArgumentType.string())
                                .executes(this::setDisplayName)));

        // /core set tokens
        final var setTokensLiteral = Commands.literal("tokens")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(this::setCoins)));

        // /core set votes
        final var setVotesLiteral = Commands.literal("votes")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(this::setVotes)));


        // /core set vanish
        final var setVanish = Commands.literal("vanish")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(this::setVanish)));

        // /core set chatspy
        final var setChatSpy = Commands.literal("chatspy")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(this::setChatSpy)));

        // /core set confirmed
        final var setConfirmed = Commands.literal("confirmed")
                .then(Commands.literal("all")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(this::setConfirmedAll)))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(this::setConfirmed)));

        // /core set
        final var setLiteral = Commands.literal("set")
                .then(setAllowTpa)
                .then(setDisplayName)
                .then(setConfirmed)
                .then(setTokensLiteral)
                .then(setVotesLiteral)
                .then(setVanish)
                .then(setChatSpy);

        // /core add tokens
        final var addTokensLiteral = Commands.literal("tokens")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("addAmount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(this::addCoins)));

        // /core add votes
        final var addVotesLiteral = Commands.literal("votes")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("addAmount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(this::addVotes)));

        // /core add
        final var addLiteral = Commands.literal("add")
                .then(addTokensLiteral)
                .then(addVotesLiteral);

        // /core subtract tokens
        final var subtractTokensLiteral = Commands.literal("tokens")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("subAmount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(this::subtractTokens)));

        // /core subtract votes
        final var subtractVotesLiteral = Commands.literal("votes")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .then(Commands.argument("subAmount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                .executes(this::subtractVotes)));

        // /core subtract
        final var subtractLiteral = Commands.literal("subtract")
                .then(subtractTokensLiteral)
                .then(subtractVotesLiteral);

        // /core queue
        final var queueLiteral = Commands.literal("queue")
                .then(Commands.literal("all")
                        .executes(this::queueAll))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(new CorePlayerSuggestionProvider(this.plugin, true, false))
                        .executes(this::queue));

        // /core
        final var command = Commands.literal("core")
                .requires(stack -> stack.getSender().hasPermission(CorePermission.COMMAND_CORE.get()))
                .then(debugLiteral)
                .then(redisLiteral)
                .then(reloadLiteral)
                .then(resetLiteral)
                .then(saveLiteral)
                .then(setLiteral)
                .then(addLiteral)
                .then(subtractLiteral)
                .then(queueLiteral)
                .build();

        registrar.register(command, "Manage the core plugin");
    }

    // Toggles debug mode
    public int debug(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();

        // Toggle debug mode
        boolean debug = this.plugin.isDebugMode();
        this.plugin.getConfigController().setDebugMode(!(debug));
        this.plugin.setDebugMode(!(debug));

        final String message = debug
                ? "command-core-debug-disable"
                : "command-core-debug-enable";

        // Send result message to the sender
        sender.sendMessage(this.plugin.getConfigController().getMessage(message));

        return Command.SINGLE_SUCCESS;
    }

    // Gets a value from redis
    public int redisGet(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String key = StringArgumentType.getString(context, "key");

        // Run task asynchronously
        this.plugin.getRedisController().get(key).thenAccept(value -> {
            if (value != null) {
                // Send result message to the sender
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-redis-get-result",
                        Placeholder.parsed("value", value)));
            } else {
                // Send no result message to the sender
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-redis-get-none"));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    // Searches for keys in redis
    public int redisSearch(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String key = StringArgumentType.getString(context, "key");

        // Run task asynchronously
        this.plugin.getRedisController().getKeys(key).thenAccept(list -> {
            if (list != null && !list.isEmpty()) {
                // Send header message to the sender
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-redis-search",
                        Placeholder.parsed("amount", String.valueOf(list.size()))));

                for (final String foundKey : list) {
                    // Send each search result to the sender
                    sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-redis-search-result",
                            Placeholder.parsed("key", foundKey)));
                }
            } else {
                // Send no results message to the sender
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-redis-search-none"));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    // Reloads the config
    public int reload(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();

        // Reload the config
        this.plugin.reload();
        final boolean success = this.plugin.getConfigController().getPrimaryNode() != null
                && this.plugin.getConfigController().getMessagesNode() != null;

        // Send a message to the sender
        sender.sendMessage(this.plugin.getConfigController().getMessage(success
                ? "command-core-reload-success"
                : "command-core-reload-fail"));
        return Command.SINGLE_SUCCESS;
    }

    // Resets a player
    public int reset(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final PaperCorePlayer corePlayer = (PaperCorePlayer) this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            // Reset a player's data
            corePlayer.setServer(this.plugin.getCoreServer());
            corePlayer.setChannels(corePlayer.createNewChannelMap());
            corePlayer.setStanding(CoreStanding.EXCELLENT);
            corePlayer.setRecent(corePlayer.getUniqueId());
            corePlayer.setDisplayName(this.plugin.getConfigController().getMessage("default-display-name",
                    Placeholder.parsed("username", username)));
            corePlayer.setNickname(Component.empty());
            corePlayer.setFriends(new ArrayList<>());
            corePlayer.setBlocked(new ArrayList<>());
            corePlayer.setOnline(corePlayer.isOnline(), false);
            corePlayer.setVanished(false);
            corePlayer.setSpying(false);
            corePlayer.setAllowTpa(true);
            corePlayer.setAllowMention(true);
            corePlayer.setConfirmed(false, false);
            corePlayer.setCoins(0);
            corePlayer.setVotes(0);
            corePlayer.setMeows(0);

            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-reset",
                    Placeholder.parsed("player", username)));
        } else {
            // Player was not found in cache
            sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
        }
        return Command.SINGLE_SUCCESS;
    }

    // Saves a player to the database
    public int save(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            // Save player to the database
            this.plugin.getDataController().savePlayer(corePlayer);
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-save",
                    Placeholder.parsed("player", username)));
        } else {
            // Player was not found in cache
            sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
        }
        return Command.SINGLE_SUCCESS;
    }

    // Saves all cached players to the database
    public int saveAll(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final Collection<? extends CorePlayer> online = this.plugin.getPlayerController().getPlayers();

        // Save all online players to the database
        for (final CorePlayer corePlayer : online) {
            this.plugin.getDataController().savePlayer(corePlayer);
        }

        // Send a message to the sender
        sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-save-all",
                Placeholder.parsed("count", String.valueOf(online.size()))));
        return Command.SINGLE_SUCCESS;
    }

    // Manage player coins - set new value
    public int setCoins(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final int newAmount = context.getArgument("amount", Integer.class);

        this.plugin.getPlayerController().getPlayerAsync(username).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                // Player found, set new value
                final int oldAmount = corePlayer.getCoins();
                corePlayer.setCoins(newAmount);
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-coins",
                        Placeholder.parsed("player", corePlayer.getName()),
                        Placeholder.parsed("old_amount", String.valueOf(oldAmount)),
                        Placeholder.parsed("new_amount", String.valueOf(newAmount))));
            } else {
                // Player not found
                sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    // Manage player votes - set new value
    public int setVotes(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final int newAmount = context.getArgument("amount", Integer.class);

        this.plugin.getPlayerController().getPlayerAsync(username).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                // Player found, set new value
                final int oldAmount = corePlayer.getVotes();
                corePlayer.setVotes(newAmount);
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-votes",
                        Placeholder.parsed("player", corePlayer.getName()),
                        Placeholder.parsed("old_amount", String.valueOf(oldAmount)),
                        Placeholder.parsed("new_amount", String.valueOf(newAmount))));
            } else {
                // Player not found
                sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    // Manage player coins - add amount to current value
    public int addCoins(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final int modifier = context.getArgument("addAmount", Integer.class);

        this.plugin.getPlayerController().getPlayerAsync(username).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                // Player found, set new value
                final int oldAmount = corePlayer.getCoins();
                final int newAmount = oldAmount + modifier;
                corePlayer.setCoins(newAmount);
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-add-coins",
                        Placeholder.parsed("player", corePlayer.getName()),
                        Placeholder.parsed("modifier", String.valueOf(modifier)),
                        Placeholder.parsed("old_amount", String.valueOf(oldAmount)),
                        Placeholder.parsed("new_amount", String.valueOf(newAmount))));
            } else {
                // Player not found
                sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    // Manage player votes - add amount to current value
    public int addVotes(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final int modifier = context.getArgument("addAmount", Integer.class);

        this.plugin.getPlayerController().getPlayerAsync(username).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                // Player found, set new value
                final int oldAmount = corePlayer.getVotes();
                final int newAmount = oldAmount + modifier;
                corePlayer.setVotes(newAmount);
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-add-votes",
                        Placeholder.parsed("player", corePlayer.getName()),
                        Placeholder.parsed("modifier", String.valueOf(modifier)),
                        Placeholder.parsed("old_amount", String.valueOf(oldAmount)),
                        Placeholder.parsed("new_amount", String.valueOf(newAmount))));
            } else {
                // Player not found
                sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    // Manage player coins - subtract amount from current value
    public int subtractTokens(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final int modifier = context.getArgument("subAmount", Integer.class);

        this.plugin.getPlayerController().getPlayerAsync(username).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                // Player found, set new value
                final int oldAmount = corePlayer.getCoins();
                final int newAmount = oldAmount - modifier;
                corePlayer.setCoins(newAmount);
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-subtract-coins",
                        Placeholder.parsed("player", corePlayer.getName()),
                        Placeholder.parsed("modifier", String.valueOf(modifier)),
                        Placeholder.parsed("old_amount", String.valueOf(oldAmount)),
                        Placeholder.parsed("new_amount", String.valueOf(newAmount))));
            } else {
                // Player not found
                sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    // Manage player votes - subtract amount from current value
    public int subtractVotes(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final int modifier = context.getArgument("subAmount", Integer.class);

        this.plugin.getPlayerController().getPlayerAsync(username).thenAccept(corePlayer -> {
            if (corePlayer != null) {
                // Player found, set new value
                final int oldAmount = corePlayer.getVotes();
                final int newAmount = oldAmount - modifier;
                corePlayer.setVotes(newAmount);
                sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-subtract-votes",
                        Placeholder.parsed("player", corePlayer.getName()),
                        Placeholder.parsed("modifier", String.valueOf(modifier)),
                        Placeholder.parsed("old_amount", String.valueOf(oldAmount)),
                        Placeholder.parsed("new_amount", String.valueOf(newAmount))));
            } else {
                // Player not found
                sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    // Manage player tpa toggle
    public int setAllowTpa(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final boolean value = context.getArgument("value", Boolean.class);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            corePlayer.setAllowTpa(value);

            // Send a message to the sender
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-allow-tpa",
                    Placeholder.parsed("player", corePlayer.getName()),
                    Placeholder.parsed("value", String.valueOf(value))));
        } else {
            // Player was not found in cache
            this.plugin.getDataController().queryPlayer(username).thenAccept(dbPlayer -> {
                if (dbPlayer != null) {
                    // Player was found in the database
                    dbPlayer.setAllowTpa(value);

                    // Send a message to the sender
                    sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-allow-tpa",
                            Placeholder.parsed("player", dbPlayer.getName()),
                            Placeholder.parsed("value", String.valueOf(value))));
                } else {
                    // Player does not exist
                    sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    // Manage player confirmation for all players
    public int setConfirmedAll(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final boolean value = context.getArgument("value", Boolean.class);

        // Set confirmation
        if (value) {
            this.plugin.getPlayerController().confirmAll();
        } else {
            this.plugin.getPlayerController().unconfirmAll();
        }

        // Send a message to the sender
        sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-confirmed-all",
                Placeholder.parsed("value", String.valueOf(value))));

        return Command.SINGLE_SUCCESS;
    }

    // Manage player confirmation
    public int setConfirmed(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final boolean value = context.getArgument("value", Boolean.class);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            corePlayer.setConfirmed(value, false);

            // Send a message to the sender
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-confirmed",
                    Placeholder.parsed("player", corePlayer.getName()),
                    Placeholder.parsed("value", String.valueOf(value))));
        } else {
            // Player was not found in cache
            this.plugin.getDataController().queryPlayer(username).thenAccept(dbPlayer -> {
                if (dbPlayer != null) {
                    // Player was found in the database
                    dbPlayer.setConfirmed(value, false);

                    // Send a message to the sender
                    sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-confirmed",
                            Placeholder.parsed("player", dbPlayer.getName()),
                            Placeholder.parsed("value", String.valueOf(value))));
                } else {
                    // Player does not exist
                    sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    // Manage player display name
    public int setDisplayName(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final String value = context.getArgument("value", String.class);
        final Component displayName = this.plugin.getMiniMessage().deserialize(value);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            corePlayer.setDisplayName(this.plugin.getMiniMessage().deserialize(value));

            // Send a message to the sender
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-display-name",
                    Placeholder.parsed("player", corePlayer.getName()),
                    Placeholder.component("display_name", displayName)));
        } else {
            // Player was not found in cache
            this.plugin.getDataController().queryPlayer(username).thenAccept(dbPlayer -> {
                if (dbPlayer != null) {
                    // Player was found in the database
                    dbPlayer.setDisplayName(this.plugin.getMiniMessage().deserialize(value));

                    // Send a message to the sender
                    sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-display-name",
                            Placeholder.parsed("player", dbPlayer.getName()),
                            Placeholder.component("display_name", displayName)));
                } else {
                    // Player does not exist
                    sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    // Manage player vanish
    public int setVanish(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final boolean value = context.getArgument("value", Boolean.class);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            corePlayer.setVanished(value);

            // Send a message to the sender
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-vanish",
                    Placeholder.parsed("player", corePlayer.getName()),
                    Placeholder.parsed("value", String.valueOf(value))));
        } else {
            // Player was not found in cache
            this.plugin.getDataController().queryPlayer(username).thenAccept(dbPlayer -> {
                if (dbPlayer != null) {
                    // Player was found in the database
                    dbPlayer.setVanished(value);

                    // Send a message to the sender
                    sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-vanish",
                            Placeholder.parsed("player", dbPlayer.getName()),
                            Placeholder.parsed("value", String.valueOf(value))));
                } else {
                    // Player does not exist
                    sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    // Manage player chat spy
    public int setChatSpy(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final boolean value = context.getArgument("value", Boolean.class);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            corePlayer.setSpying(value);

            // Send a message to the sender
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-chat-spy",
                    Placeholder.parsed("player", corePlayer.getName()),
                    Placeholder.parsed("value", String.valueOf(value))));
        } else {
            // Player was not found in cache
            this.plugin.getDataController().queryPlayer(username).thenAccept(dbPlayer -> {
                if (dbPlayer != null) {
                    // Player was found in the database
                    dbPlayer.setSpying(value);

                    // Send a message to the sender
                    sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-set-chat-spy",
                            Placeholder.parsed("player", dbPlayer.getName()),
                            Placeholder.parsed("value", String.valueOf(value))));
                } else {
                    // Player does not exist
                    sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    // Adds a player to the queue
    public int queue(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final String username = context.getArgument("player", String.class);
        final CorePlayer corePlayer = this.plugin.getPlayerController().getPlayer(username);

        // Check if player is in cache
        if (corePlayer != null) {
            // Add player to queue
            this.plugin.getPlayerQueueTask().queue((PaperCorePlayer) corePlayer);
            sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-queue",
                    Placeholder.parsed("player", username)));
        } else {
            // Player was not found in cache
            sender.sendMessage(this.plugin.getConfigController().getMessage("error-player-not-found"));
        }
        return Command.SINGLE_SUCCESS;
    }

    // Adds all cached players to the queue
    public int queueAll(final CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        final Collection<? extends CorePlayer> online = this.plugin.getPlayerController().getPlayers();

        // Queue all cached players
        for (final CorePlayer corePlayer : online) {
            this.plugin.getPlayerQueueTask().queue((PaperCorePlayer) corePlayer);
        }

        // Send a message to the sender
        sender.sendMessage(this.plugin.getConfigController().getMessage("command-core-queue-all",
                Placeholder.parsed("count", String.valueOf(online.size()))));
        return Command.SINGLE_SUCCESS;
    }
}
