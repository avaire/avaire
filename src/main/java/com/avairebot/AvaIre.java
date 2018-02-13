package com.avairebot;

import com.avairebot.ai.IntelligenceManager;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.cache.CacheManager;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandHandler;
import com.avairebot.config.Configuration;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.reflection.Reflectionable;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.migrate.migrations.*;
import com.avairebot.database.serializer.PlaylistSongSerializer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.exceptions.InvalidApplicationEnvironmentException;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.exceptions.InvalidPluginsPathException;
import com.avairebot.factories.MessageFactory;
import com.avairebot.handlers.GenericEventHandler;
import com.avairebot.handlers.MainEventHandler;
import com.avairebot.metrics.Metrics;
import com.avairebot.plugin.PluginLoader;
import com.avairebot.plugin.PluginManager;
import com.avairebot.scheduler.ScheduleHandler;
import com.avairebot.shard.ShardEntityCounter;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.shared.ExitCodes;
import com.avairebot.time.Carbon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import io.sentry.Sentry;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

public class AvaIre {

    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(
            PlaylistTransformer.PlaylistSong.class,
            new PlaylistSongSerializer()
        )
        .disableHtmlEscaping()
        .serializeNulls()
        .create();

    private static final Logger LOGGER = LoggerFactory.getLogger(AvaIre.class);
    private static Environment APPLICATION_ENVIRONMENT;
    private final Settings settings;
    private final Configuration config;
    private final CacheManager cache;
    private final DatabaseManager database;
    private final IntelligenceManager intelligenceManager;
    private final PluginManager pluginManager;
    private final ShardEntityCounter shardEntityCounter;

    private Carbon shutdownTime = null;
    private int shutdownCode = ExitCodes.EXIT_CODE_RESTART;

    private ShardManager shardManager = null;

    public AvaIre(Settings settings) throws IOException, SQLException, InvalidApplicationEnvironmentException {
        this.settings = settings;

        System.out.println(getVersionInfo());

        LOGGER.debug("====================================================");
        LOGGER.debug("Starting the application with debug logging enabled!");
        LOGGER.debug("====================================================\n");

        LOGGER.info("Bootstrapping AvaIre v" + AppInfo.getAppInfo().VERSION);
        Reflections.log = null;

        this.cache = new CacheManager(this);

        LOGGER.info("Loading configuration");
        config = new Configuration(this, null, "config.yml");
        if (!config.exists()) {
            getLogger().info("The {} configuration file is missing!", "config.yml");
            getLogger().info("Creating file and terminating program...");

            config.saveDefaultConfig();

            System.exit(ExitCodes.EXIT_CODE_NORMAL);
        }

        APPLICATION_ENVIRONMENT = Environment.fromName(config.getString("environment", "production"));
        if (APPLICATION_ENVIRONMENT == null) {
            throw new InvalidApplicationEnvironmentException(config.getString("environment", "production"));
        }
        LOGGER.info("Starting application in \"{}\" mode", APPLICATION_ENVIRONMENT.getName());

        LOGGER.info("Registering and connecting to database");
        database = new DatabaseManager(this);

        LOGGER.info("Registering database table migrations");
        database.getMigrations().register(
            new CreateGuildTableMigration(),
            new CreateGuildTypeTableMigration(),
            new CreateBlacklistTableMigration(),
            new CreatePlayerExperiencesTableMigration(),
            new CreateFeedbackTableMigration(),
            new CreateMusicPlaylistsTableMigration(),
            new CreateStatisticsTableMigration(),
            new CreateShardsTableMigration(),
            new AddDJLevelToGuildsTableMigration(),
            new RenamePlaylistSizeColumnToAmountMigration(),
            new AddModlogToGuildsTableMigration()
        );

        LOGGER.info("Registering default command categories");
        String defaultPrefix = getConfig().getString("default-prefix", DiscordConstants.DEFAULT_COMMAND_PREFIX);
        CategoryHandler.addCategory(this, "Administration", defaultPrefix);
        CategoryHandler.addCategory(this, "Help", defaultPrefix);
        CategoryHandler.addCategory(this, "Fun", defaultPrefix);
        CategoryHandler.addCategory(this, "Interaction", defaultPrefix);
        CategoryHandler.addCategory(this, "Music", defaultPrefix);
        CategoryHandler.addCategory(this, "Search", defaultPrefix);
        CategoryHandler.addCategory(this, "Utility", defaultPrefix);
        CategoryHandler.addCategory(this, "System", ";");

        LOGGER.info("Registering commands...");
        autoloadPackage(Constants.PACKAGE_COMMAND_PATH, command -> CommandHandler.register((Command) command));
        LOGGER.info(String.format("\tRegistered %s commands successfully!", CommandHandler.getCommands().size()));

        LOGGER.info("Registering jobs...");
        autoloadPackage(Constants.PACKAGE_JOB_PATH, job -> ScheduleHandler.registerJob((Job) job));
        LOGGER.info(String.format("\tRegistered %s jobs successfully!", ScheduleHandler.entrySet().size()));

        intelligenceManager = new IntelligenceManager(this);
        if (intelligenceManager.isEnabled()) {
            LOGGER.info("Registering intents...");
            autoloadPackage(Constants.PACKAGE_INTENTS_PATH, intent -> intelligenceManager.registerIntent((Intent) intent));
            LOGGER.info(String.format("\tRegistered %s intelligence intents successfully!", intelligenceManager.entrySet().size()));
        }

        LOGGER.info("Creating plugin manager and registering plugins...");
        pluginManager = new PluginManager(this);

        try {
            pluginManager.loadPlugins();

            if (pluginManager.getPlugins().isEmpty()) {
                LOGGER.info("\tNo plugins was found");
            } else {
                LOGGER.info(String.format("\t%s plugins was loaded, invoking all plugins", pluginManager.getPlugins().size()));
                for (PluginLoader plugin : pluginManager.getPlugins()) {
                    int commands = CommandHandler.getCommands().size();
                    int categories = CategoryHandler.getValues().size();
                    int migrations = getDatabase().getMigrations().getMigrations().size();

                    plugin.invokePlugin(this);

                    LOGGER.info("\t\t\"{}\" has been enabled with {} Command(s), {} Command Categories, {} Database Migration(s)",
                        plugin.getName(),
                        CommandHandler.getCommands().size() - commands,
                        CategoryHandler.getValues().size() - categories,
                        getDatabase().getMigrations().getMigrations().size() - migrations
                    );
                }
            }
        } catch (InvalidPluginsPathException | InvalidPluginException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(ExitCodes.EXIT_CODE_ERROR);
        }

        LOGGER.info("Running database migrations");
        database.getMigrations().up();

        LOGGER.info("Preparing and setting up metrics");
        Metrics.setup(this);

        String sentryDsn = config.getString("sentryDsn", "").trim();
        if (sentryDsn.length() > 0) {
            LOGGER.info("SentryDSN found, initializing Sentry.io");
            Sentry.init(sentryDsn);

            Sentry.getStoredClient().setEnvironment(getEnvironment().getName());

            switch (getEnvironment()) {
                case PRODUCTION:
                    Sentry.getStoredClient().setRelease(GitInfo.getGitInfo().commitId);
                    break;

                default:
                    Sentry.getStoredClient().setRelease(AppInfo.getAppInfo().VERSION);
                    break;
            }
        }

        LOGGER.info("Preparing Lavalink");
        LavalinkManager.LavalinkManagerHolder.LAVALINK.start(this);

        LOGGER.info("Creating bot instance and connecting to Discord network");

        shardEntityCounter = new ShardEntityCounter(this);

        try {
            shardManager = buildShardManager();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Environment getEnvironment() {
        return APPLICATION_ENVIRONMENT;
    }

    static String getVersionInfo() {
        return "\n\n" +
            "     ___   ____    ____  ___       __  .______       _______ \n" +
            "    /   \\  \\   \\  /   / /   \\     |  | |   _  \\     |   ____|\n" +
            "   /  ^  \\  \\   \\/   / /  ^  \\    |  | |  |_)  |    |  |__   \n" +
            "  /  /_\\  \\  \\      / /  /_\\  \\   |  | |      /     |   __|  \n" +
            " /  _____  \\  \\    / /  _____  \\  |  | |  |\\  \\----.|  |____ \n" +
            "/__/     \\__\\  \\__/ /__/     \\__\\ |__| | _| `._____||_______|\n" +
            ""
            + "\n\tVersion:       " + AppInfo.getAppInfo().VERSION
            + "\n\tJVM:           " + System.getProperty("java.version")
            + "\n\tJDA:           " + JDAInfo.VERSION
            + "\n\tLavaplayer     " + PlayerLibrary.VERSION
            + "\n";
    }

    /**
     * Checks if we're ready yet by checking if all the shards are connected and ready to serve events.
     *
     * @return <code>True</code> if all shards has connected and are ready, <code>False</code> otherwise.
     */
    public boolean areWeReadyYet() {
        for (JDA shard : getShardManager().getShards()) {
            if (shard.getStatus() != JDA.Status.CONNECTED) {
                return false;
            }
        }
        return true;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public ShardEntityCounter getShardEntityCounter() {
        return shardEntityCounter;
    }

    public SelfUser getSelfUser() {
        return getShardManager().getShards().get(0).getSelfUser();
    }

    public Settings getSettings() {
        return settings;
    }

    public Configuration getConfig() {
        return config;
    }

    public CacheManager getCache() {
        return cache;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public IntelligenceManager getIntelligenceManager() {
        return intelligenceManager;
    }

    public void shutdown() {
        shutdown(ExitCodes.EXIT_CODE_RESTART);
    }

    public void shutdown(int exitCode) {
        getLogger().info("Shutting down bot instance gracefully with exit code " + exitCode);

        long waitFor = 1000;
        for (GuildMusicManager manager : AudioHandler.MUSIC_MANAGER.values()) {
            if (manager.getLastActiveMessage() != null) {
                MessageFactory.makeInfo(manager.getLastActiveMessage(),
                    "Bot is restarting, sorry for the inconvenience, we'll be right back!"
                ).queue();

                waitFor += 500;
            }
        }

        try {
            Thread.sleep(waitFor);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (JDA shard : getShardManager().getShards()) {
            shard.shutdown();
        }

        for (Map.Entry<String, ScheduledFuture<?>> job : ScheduleHandler.entrySet()) {
            job.getValue().cancel(true);
        }

        try {
            getDatabase().getConnection().close();
        } catch (SQLException ex) {
            getLogger().error("Failed to close database connection during shutdown: ", ex);
        }

        System.exit(exitCode);
    }

    public void scheduleShutdown(Carbon time, int exitCode) {
        shutdownTime = time;
        shutdownCode = exitCode;
    }

    public Carbon getShutdownTime() {
        return shutdownTime;
    }

    public int getShutdownCode() {
        return shutdownCode;
    }

    private ShardManager buildShardManager() throws LoginException {
        DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder()
            .setSessionController(new SessionControllerAdapter())
            .setToken(getConfig().getString("discord.token"))
            .setGame(Game.watching("my code start up..."))
            .setBulkDeleteSplittingEnabled(false)
            .setEnableShutdownHook(false)
            .setAutoReconnect(true)
            .setAudioEnabled(true)
            .setContextEnabled(true)
            .setShardsTotal(settings.getShardCount());

        builder
            .addEventListeners(new MainEventHandler(this))
            .addEventListeners(new GenericEventHandler(this));

        if (LavalinkManager.LavalinkManagerHolder.LAVALINK.isEnabled()) {
            builder.addEventListeners(LavalinkManager.LavalinkManagerHolder.LAVALINK);
        }

        for (PluginLoader plugin : getPluginManager().getPlugins()) {
            for (ListenerAdapter listener : plugin.getEventListeners()) {
                builder.addEventListeners(listener);
            }
        }

        return builder.build();
    }

    private void autoloadPackage(String path, Consumer<Reflectionable> callback) {
        Set<Class<? extends Reflectionable>> types = new Reflections(path).getSubTypesOf(Reflectionable.class);

        Class[] arguments = new Class[1];
        arguments[0] = AvaIre.class;

        for (Class<? extends Reflectionable> reflectionClass : types) {
            if (reflectionClass.getPackage().getName().contains("contracts")) {
                continue;
            }

            try {
                callback.accept(reflectionClass.getDeclaredConstructor(arguments).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                getLogger().error("Failed to create a new instance of package {}", reflectionClass.getName(), e);
            }
        }
    }
}
