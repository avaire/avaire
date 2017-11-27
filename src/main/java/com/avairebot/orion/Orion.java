package com.avairebot.orion;

import com.avairebot.orion.ai.IntelligenceManager;
import com.avairebot.orion.ai.intents.RequestOnlinePlayers;
import com.avairebot.orion.ai.intents.SmallTalk;
import com.avairebot.orion.ai.intents.Unknown;
import com.avairebot.orion.cache.CacheManager;
import com.avairebot.orion.commands.CategoryHandler;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.administration.*;
import com.avairebot.orion.commands.fun.*;
import com.avairebot.orion.commands.help.HelpCommand;
import com.avairebot.orion.commands.interaction.*;
import com.avairebot.orion.commands.music.*;
import com.avairebot.orion.commands.system.EvalCommand;
import com.avairebot.orion.commands.system.SetStatusCommand;
import com.avairebot.orion.commands.utility.*;
import com.avairebot.orion.config.ConfigurationLoader;
import com.avairebot.orion.config.MainConfiguration;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.migrate.migrations.*;
import com.avairebot.orion.exceptions.InvalidPluginException;
import com.avairebot.orion.exceptions.InvalidPluginsPathException;
import com.avairebot.orion.plugin.PluginLoader;
import com.avairebot.orion.plugin.PluginManager;
import com.avairebot.orion.scheduler.*;
import net.dv8tion.jda.core.entities.SelfUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Orion {

    private static final Logger LOGGER = LoggerFactory.getLogger(Orion.class);

    private static final List<OrionShard> SHARDS = new CopyOnWriteArrayList<>();

    private final MainConfiguration config;
    private final CacheManager cache;
    private final DatabaseManager database;
    private final IntelligenceManager intelligenceManager;
    private final PluginManager pluginManager;

    public Orion() throws IOException, SQLException {
        LOGGER.info("Bootstrapping Orion v" + AppInfo.getAppInfo().VERSION + " Build " + AppInfo.getAppInfo().BUILD_NUMBER);

        this.cache = new CacheManager(this);

        LOGGER.info(" - Loading configuration");
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            LOGGER.error("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }

        LOGGER.info(" - Registering and connecting to database");
        database = new DatabaseManager(this);

        LOGGER.info(" - Registering database table migrations");
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
        database.getMigrations().up();

        LOGGER.info(" - Registering default command categories");
        CategoryHandler.addCategory("Administration", ".");
        CategoryHandler.addCategory("Help", ".");
        CategoryHandler.addCategory("Fun", ">");
        CategoryHandler.addCategory("Interaction", ">");
        CategoryHandler.addCategory("Music", "!");
        CategoryHandler.addCategory("Utility", "!");
        CategoryHandler.addCategory("System", ";");

        this.registerCommands();
        this.registerJobs();

        intelligenceManager = new IntelligenceManager(this);
        if (intelligenceManager.isEnabled()) {
            this.registerIntents();
        }

        LOGGER.info(" - Creating plugin manager and registering plugins...");
        pluginManager = new PluginManager(this);

        try {
            pluginManager.loadPlugins();

            if (pluginManager.getPlugins().isEmpty()) {
                LOGGER.info(" - No plugins was found");
            } else {
                LOGGER.info(String.format(" - %s plugins was loaded, invoking all plugins", pluginManager.getPlugins().size()));
                for (PluginLoader plugin : pluginManager.getPlugins()) {
                    plugin.invokePlugin(this);
                }
            }
        } catch (InvalidPluginsPathException | InvalidPluginException e) {
            e.printStackTrace();
            System.exit(0);
        }

        LOGGER.info(" - Creating bot instance and connecting to Discord network");
        if (getConfig().botAuth().getShardsTotal() < 1) {
            SHARDS.add(new OrionShard(this, 0));
            return;
        }

        for (int i = 0; i < 2; i++) {
            SHARDS.add(new OrionShard(this, i));
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public List<OrionShard> getShards() {
        return SHARDS;
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

    public IntelligenceManager getIntelligenceManager() {
        return intelligenceManager;
    }

    public long getUserCount() {
        long count = 0;
        for (OrionShard shard : SHARDS) {
            count += shard.getJDA().getUsers().size();
        }
        return count;
    }

    public long getTextChannelCount() {
        long count = 0;
        for (OrionShard shard : SHARDS) {
            count += shard.getJDA().getTextChannels().size();
        }
        return count;
    }

    public long getVoiceChannelCount() {
        long count = 0;
        for (OrionShard shard : SHARDS) {
            count += shard.getJDA().getVoiceChannels().size();
        }
        return count;
    }

    public long getChannelCount() {
        return getTextChannelCount() + getVoiceChannelCount();
    }

    public long getGuildCount() {
        long count = 0;
        for (OrionShard shard : SHARDS) {
            count += shard.getJDA().getGuilds().size();
        }
        return count;
    }

    private void registerCommands() {
        LOGGER.info(" - Registering commands...");

        // Administration
        CommandHandler.register(new AddSelfAssignableRoleCommand(this));
        CommandHandler.register(new AiCommand(this));
        CommandHandler.register(new AliasCommand(this));
        CommandHandler.register(new AutoAssignRoleCommand(this));
        CommandHandler.register(new BanCommand(this));
        CommandHandler.register(new ChangePrefixCommand(this));
        CommandHandler.register(new ChannelIdCommand(this));
        CommandHandler.register(new ChannelInfoCommand(this));
        CommandHandler.register(new GoodbyeCommand(this));
        CommandHandler.register(new GoodbyeMessageCommand(this));
        CommandHandler.register(new IAmCommand(this));
        CommandHandler.register(new IAmNotCommand(this));
        CommandHandler.register(new KickCommand(this));
        CommandHandler.register(new LevelAlertsCommand(this));
        CommandHandler.register(new LevelCommand(this));
        CommandHandler.register(new ListAliasesCommand(this));
        CommandHandler.register(new ListSelfAssignableRolesCommand(this));
        CommandHandler.register(new PurgeCommand(this));
        CommandHandler.register(new RemoveSelfAssignableRoleCommand(this));
        CommandHandler.register(new ServerIdCommand(this));
        CommandHandler.register(new ServerInfoCommand(this));
        CommandHandler.register(new SoftBanCommand(this));
        CommandHandler.register(new UserIdCommand(this));
        CommandHandler.register(new UserInfoCommand(this));
        CommandHandler.register(new VoiceKickCommand(this));
        CommandHandler.register(new WelcomeCommand(this));
        CommandHandler.register(new WelcomeMessageCommand(this));

        // Fun
        CommandHandler.register(new ChuckNorrisCommand(this));
        CommandHandler.register(new CoinflipCommand(this));
        CommandHandler.register(new DiceCommand(this));
        CommandHandler.register(new EightBallCommand(this));
        CommandHandler.register(new GfycatCommand(this));
        CommandHandler.register(new LennyCommand(this));
        CommandHandler.register(new MemeCommand(this));
        CommandHandler.register(new RandomCatCommand(this));
        CommandHandler.register(new RandomDogCommand(this));
        CommandHandler.register(new RepeatCommand(this));
        CommandHandler.register(new RipCommand(this));
        CommandHandler.register(new RollCommand(this));
        CommandHandler.register(new SayCommand(this));
        CommandHandler.register(new UrbanDictionaryCommand(this));
        CommandHandler.register(new VoteSkipCommand(this));
        CommandHandler.register(new XKCDCommand(this));

        // Help/Support
        CommandHandler.register(new HelpCommand(this));

        // Interactions
        CommandHandler.register(new BiteCommand(this));
        CommandHandler.register(new CuddleCommand(this));
        CommandHandler.register(new DivorceCommand(this));
        CommandHandler.register(new HelloCommand(this));
        CommandHandler.register(new HighFiveCommand(this));
        CommandHandler.register(new HugCommand(this));
        CommandHandler.register(new KillCommand(this));
        CommandHandler.register(new KissCommand(this));
        CommandHandler.register(new PanCommand(this));
        CommandHandler.register(new PatCommand(this));
        CommandHandler.register(new PokeCommand(this));
        CommandHandler.register(new PunchCommand(this));
        CommandHandler.register(new SenpaiCommand(this));
        CommandHandler.register(new SlapCommand(this));
        CommandHandler.register(new TickleCommand(this));

        // Music
        CommandHandler.register(new ClearQueueCommand(this));
        CommandHandler.register(new MoveHereCommand(this));
        CommandHandler.register(new PauseCommand(this));
        CommandHandler.register(new PlayCommand(this));
        CommandHandler.register(new RepeatMusicQueueCommand(this));
        CommandHandler.register(new ResumeCommand(this));
        CommandHandler.register(new SeekCommand(this));
        CommandHandler.register(new SkipCommand(this));
        CommandHandler.register(new SongCommand(this));
        CommandHandler.register(new VolumeCommand(this));

        // System
        CommandHandler.register(new EvalCommand(this));
        CommandHandler.register(new SetStatusCommand(this));

        // Utility
        CommandHandler.register(new DuckDuckGoCommand(this));
        CommandHandler.register(new ExpandUrlCommand(this));
        CommandHandler.register(new FeedbackCommand(this));
        CommandHandler.register(new GlobalLeaderboardCommand(this));
        CommandHandler.register(new InviteCommand(this));
        CommandHandler.register(new IPInfoCommand(this));
        CommandHandler.register(new LeaderboardCommand(this));
        CommandHandler.register(new PingCommand(this));
        CommandHandler.register(new RankCommand(this));
        CommandHandler.register(new SourceCommand(this));
        CommandHandler.register(new StatsCommand(this));

        LOGGER.info(String.format(" - Registered %s commands successfully!", CommandHandler.getCommands().size()));
    }

    private void registerJobs() {
        LOGGER.info(" - Registering jobs...");

        ScheduleHandler.registerJob(new ChangeGameJob(this));
        ScheduleHandler.registerJob(new GithubChangesJob(this));
        ScheduleHandler.registerJob(new FetchMemeTypesJob(this));
        ScheduleHandler.registerJob(new GarbageCollectorJob(this));
        ScheduleHandler.registerJob(new ResetRespectStatisticsJob(this));

        LOGGER.info(String.format(" - Registered %s jobs successfully!", ScheduleHandler.entrySet().size()));
    }

    private void registerIntents() {
        LOGGER.info(" - Registering intents...");

        intelligenceManager.registerIntent(new Unknown(this));
        intelligenceManager.registerIntent(new SmallTalk(this));
        intelligenceManager.registerIntent(new RequestOnlinePlayers(this));

        LOGGER.info(String.format(" - Registered %s intelligence intents successfully!", intelligenceManager.entrySet().size()));
    }
}
