package com.avairebot.orion;

import com.avairebot.orion.ai.IntelligenceManager;
import com.avairebot.orion.cache.CacheManager;
import com.avairebot.orion.commands.CategoryHandler;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;
import com.avairebot.orion.contracts.ai.Intent;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.contracts.reflection.Reflectionable;
import com.avairebot.orion.contracts.scheduler.Job;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.migrate.migrations.*;
import com.avairebot.orion.exceptions.InvalidPluginException;
import com.avairebot.orion.exceptions.InvalidPluginsPathException;
import com.avairebot.orion.plugin.PluginLoader;
import com.avairebot.orion.plugin.PluginManager;
import com.avairebot.orion.scheduler.ScheduleHandler;
import com.avairebot.orion.shard.ConnectQueue;
import com.avairebot.orion.shard.OrionShard;
import com.avairebot.orion.shard.ShardEntityCounter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.SelfUser;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

public class Orion {

    public static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .create();

    private static final Logger LOGGER = LoggerFactory.getLogger("Orion");

    private static final List<OrionShard> SHARDS = new CopyOnWriteArrayList<>();
    private static final ConnectQueue CONNECT_QUEUE = new ConnectQueue();

    private final MainConfiguration config;
    private final CacheManager cache;
    private final DatabaseManager database;
    private final IntelligenceManager intelligenceManager;
    private final PluginManager pluginManager;

    private final ShardEntityCounter shardEntityCounter;

    public Orion() throws IOException, SQLException {
        System.out.println(getVersionInfo());

        LOGGER.info("Bootstrapping Orion v" + AppInfo.getAppInfo().VERSION);
        Reflections.log = null;

        this.cache = new CacheManager(this);

        LOGGER.info("Loading configuration");
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            LOGGER.error("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
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
            System.exit(0);
        }

        LOGGER.info("Running database migrations");
        database.getMigrations().up();

        LOGGER.info("Creating bot instance and connecting to Discord network");

        shardEntityCounter = new ShardEntityCounter(this);
        if (getConfig().botAuth().getShardsTotal() < 1) {
            SHARDS.add(new OrionShard(this, 0));
            return;
        }

        for (int i = 0; i < getConfig().botAuth().getShardsTotal(); i++) {
            try {
                SHARDS.add(new OrionShard(this, i));
            } catch (Exception ex) {
                getLogger().error("Caught an exception while starting shard {}!", i, ex);
                getLogger().error("Exiting program...");
                System.exit(0);
            }
        }

        getLogger().info(getShards().size() + " shards have been constructed");
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public List<OrionShard> getShards() {
        return SHARDS;
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

    public MainConfiguration getConfig() {
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

    public boolean areWeReadyYet() {
        for (OrionShard shard : getShards()) {
            if (shard.getJDA().getStatus() != JDA.Status.CONNECTED) {
                return false;
            }
        }
        return true;
    }

    public void shutdown() {
        getLogger().info("Shutting down bot instance gracefully.");
        for (OrionShard shard : getShards()) {
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

        System.exit(0);
    }

    private String getVersionInfo() {
        return "\n\n" +
            "   ____           _                        \n" +
            "  / __ \\         (_)                      \n" +
            " | |  | |  _ __   _    ___    _ __         \n" +
            " | |  | | | '__| | |  / _ \\  | '_ \\      \n" +
            " | |__| | | |    | | | (_) | | | | |       \n" +
            "  \\____/  |_|    |_|  \\___/  |_| |_|     \n"

            + "\n\tVersion:       " + AppInfo.getAppInfo().VERSION
            + "\n\tJVM:           " + System.getProperty("java.version")
            + "\n\tJDA:           " + JDAInfo.VERSION
            + "\n\tLavaplayer     " + PlayerLibrary.VERSION
            + "\n";
    }

    private void autoloadPackage(String path, Consumer<Reflectionable> callback) {
        Set<Class<? extends Reflectionable>> types = new Reflections(path).getSubTypesOf(Reflectionable.class);

        Class[] arguments = new Class[1];
        arguments[0] = Orion.class;

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
