package com.avairebot;

import com.avairebot.ai.IntelligenceManager;
import com.avairebot.cache.CacheManager;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandHandler;
import com.avairebot.config.Configuration;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.reflection.Reflectionable;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.contracts.shard.Shardable;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.migrate.migrations.*;
import com.avairebot.database.serializer.PlaylistSongSerializer;
import com.avairebot.database.transformers.PlaylistTransformer;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.exceptions.InvalidPluginsPathException;
import com.avairebot.metrics.Metrics;
import com.avairebot.plugin.PluginLoader;
import com.avairebot.plugin.PluginManager;
import com.avairebot.scheduler.ScheduleHandler;
import com.avairebot.shard.AvaireShard;
import com.avairebot.shard.ConnectQueue;
import com.avairebot.shard.ShardEntityCounter;
import com.avairebot.shared.ExitCodes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import io.sentry.Sentry;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.SelfUser;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

public class AvaIre extends Shardable {

    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(
            PlaylistTransformer.PlaylistSong.class,
            new PlaylistSongSerializer()
        )
        .disableHtmlEscaping()
        .serializeNulls()
        .create();

    private static final Logger LOGGER = LoggerFactory.getLogger(AvaIre.class);

    private static final ConnectQueue CONNECT_QUEUE = new ConnectQueue();

    private final Settings settings;
    private final Configuration config;
    private final CacheManager cache;
    private final DatabaseManager database;
    private final IntelligenceManager intelligenceManager;
    private final PluginManager pluginManager;

    private final ShardEntityCounter shardEntityCounter;

    public AvaIre(Settings settings) throws IOException, SQLException {
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
            new CreateShardsTableMigration()
        );

        LOGGER.info("Registering default command categories");
        CategoryHandler.addCategory(this, "Administration", ".");
        CategoryHandler.addCategory(this, "Help", ".");
        CategoryHandler.addCategory(this, "Fun", ">");
        CategoryHandler.addCategory(this, "Interaction", ">");
        CategoryHandler.addCategory(this, "Music", "!");
        CategoryHandler.addCategory(this, "Search", ">");
        CategoryHandler.addCategory(this, "Utility", "!");
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
            e.printStackTrace();
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
        }

        LOGGER.info("Creating bot instance and connecting to Discord network");

        shardEntityCounter = new ShardEntityCounter(this);
        if (getSettings().getShardCount() < 1) {
            SHARDS.add(new AvaireShard(this, 0));
            return;
        }

        for (int i = 0; i < getSettings().getShardCount(); i++) {
            try {
                SHARDS.add(new AvaireShard(this, i));
            } catch (Exception ex) {
                getLogger().error("Caught an exception while starting shard {}!", i, ex);
                getLogger().error("Exiting program...");
                System.exit(ExitCodes.EXIT_CODE_ERROR);
            }
        }

        getLogger().info(getShards().size() + " shards have been constructed");
    }

    public static Logger getLogger() {
        return LOGGER;
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

    public ConnectQueue getConnectQueue() {
        return CONNECT_QUEUE;
    }

    public ShardEntityCounter getShardEntityCounter() {
        return shardEntityCounter;
    }

    public SelfUser getSelfUser() {
        return getShards().get(0).getJDA().getSelfUser();
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
        for (AvaireShard shard : getShards()) {
            shard.getJDA().shutdown();
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
