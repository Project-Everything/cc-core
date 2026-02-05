package net.cc.core;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.Setter;
import net.cc.core.api.CoreProvider;
import net.cc.core.api.model.CoreChannel;
import net.cc.core.api.model.CoreServer;
import net.cc.core.command.*;
import net.cc.core.controller.*;
import net.cc.core.listener.EntityListener;
import net.cc.core.listener.PlayerListener;
import net.cc.core.model.placeholder.CorePlaceholderExpansion;
import net.cc.core.task.PlayerQueueTask;
import net.cc.core.task.PlayerSyncTask;
import net.cc.core.task.VanishTask;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class.
 *
 * @since 1.0.0
 */
@Getter
public final class CorePlugin extends JavaPlugin {

    private final ChatController chatController = new ChatController(this);
    private final ConfigController configController = new ConfigController(this);
    private final DataController dataController = new DataController(this);
    private final PlayerController playerController = new PlayerController(this);
    private final RedisController redisController = new RedisController(this);
    private final ServiceController serviceController = new ServiceController(this);

    private CoreServer coreServer;
    @Setter
    private boolean debugMode;
    private MiniMessage miniMessage;
    private MiniMessage miniMessageFull;
    private PlayerQueueTask playerQueueTask;
    private PlayerSyncTask playerSyncTask;
    private VanishTask vanishTask;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.miniMessage = MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.color())
                        .resolver(StandardTags.decorations())
                        .resolver(StandardTags.gradient())
                        .resolver(StandardTags.rainbow())
                        .resolver(StandardTags.pride())
                        .resolver(StandardTags.shadowColor())
                        .build())
                .strict(false)
                .build();

        this.miniMessageFull = MiniMessage.miniMessage();

        // Load config
        this.load();

        // Warn console about default server type
        if (this.coreServer == CoreServer.DEFAULT) {
            this.getComponentLogger().warn("Server type is set to '{}', features may not work as expected.", CoreServer.DEFAULT);
        }

        this.registerCommands();
        this.registerListeners();
        this.registerTasks();

        // Register API class
        CoreProvider.register(new CoreAPI(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.dataController.close();
        this.redisController.close();

        CoreProvider.unregister();
    }

    public void reload() {
        // Reload plugin
        this.dataController.close();
        this.redisController.close();

        this.load();
    }

    private void load() {
        // Load plugin
        this.configController.initialize();
        this.redisController.initialize();
        this.dataController.initialize();
        this.playerController.initialize();
        this.serviceController.initialize();

        // Set plugin variables
        this.coreServer = this.configController.getCoreServer();
        this.debugMode = this.configController.isDebugMode();

        this.registerPlaceholders();
    }

    private void registerCommands() {
        // Register commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands registrar = event.registrar();

            new BlockCommand(this).register(registrar);
            new BroadcastCommand(this).register(registrar);
            new ChatSpyCommand(this).register(registrar);
            new ClearChatCommand(this).register(registrar);
            new CoinsCommand(this).register(registrar);
            new CoreCommand(this).register(registrar);
            new DisplayNameCommand(this).register(registrar);
            new FakeJoinCommand(this).register(registrar);
            new FakeQuitCommand(this).register(registrar);
            new LeaderboardCommand(this).register(registrar);
            new ListCommand(this).register(registrar);
            new MeowCommand(this).register(registrar);
            new MeowsCommand(this).register(registrar);
            new MessageCommand(this).register(registrar);
            new NearCommand(this).register(registrar);
            new ReplyCommand(this).register(registrar);
            new SeenCommand(this).register(registrar);
            new StaffListCommand(this).register(registrar);
            new UnblockCommand(this).register(registrar);
            new VanishCommand(this).register(registrar);
            new VotesCommand(this).register(registrar);

            // Register channel commands
            new ChannelCommand(this, CoreChannel.STAFF_CHAT).register(registrar);
            new ChannelCommand(this, CoreChannel.HELPER_CHAT).register(registrar);
            new ChannelCommand(this, CoreChannel.MOD_CHAT).register(registrar);
            new ChannelCommand(this, CoreChannel.ADMIN_CHAT).register(registrar);

            switch (this.coreServer) {
                case PLOTS -> {
                    new NicknameCommand(this).register(registrar);
                    new ChannelCommand(this, CoreChannel.GLOBAL).register(registrar);
                    new ChannelCommand(this, CoreChannel.PLOTS_LOCAL_CHAT).register(registrar);
                }
                case EARTH -> {
                    new ChannelCommand(this, CoreChannel.EARTH_GLOBAL_CHAT).register(registrar);
                    new ChannelCommand(this, CoreChannel.EARTH_LOCAL_CHAT).register(registrar);
                    new ChannelCommand(this, CoreChannel.EARTH_TOWN_CHAT).register(registrar);
                    new ChannelCommand(this, CoreChannel.EARTH_NATION_CHAT).register(registrar);
                }
                default -> new ChannelCommand(this, CoreChannel.GLOBAL).register(registrar);
            }
        });
    }

    private void registerListeners() {
        // Register listeners
        new EntityListener(this).register();
        new PlayerListener(this).register();
    }

    private void registerPlaceholders() {
        // Register placeholders
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CorePlaceholderExpansion(this).register();
        }
    }

    private void registerTasks() {
        // Register tasks
        this.playerQueueTask = new PlayerQueueTask(this);
        this.playerSyncTask = new PlayerSyncTask(this);
        this.vanishTask = new VanishTask(this);

        // Run tasks
        this.playerQueueTask.runTaskTimer(this, Constants._30S, Constants._30S);
        this.playerSyncTask.runTaskTimer(this, Constants._1S, Constants._1S);
        this.vanishTask.runTaskTimer(this, Constants._05S, Constants._05S);
    }

}