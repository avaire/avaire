package com.avairebot.orion;

import com.avairebot.orion.cache.CacheManager;
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
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.migrate.migrations.*;
import com.avairebot.orion.handlers.EventTypes;
import com.avairebot.orion.scheduler.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Properties;

public class Orion {

    public final MainConfiguration config;
    public final SimpleLog logger;
    public final CacheManager cache;
    public final DatabaseManager database;

    private final Properties properties = new Properties();

    private JDA jda;

    public Orion() throws IOException, SQLException {
        logger = SimpleLog.getLog("Orion");
        properties.load(getClass().getClassLoader().getResourceAsStream("orion.properties"));

        logger.info("Bootstrapping Orion v" + properties.getProperty("version"));

        this.cache = new CacheManager(this);

        logger.info(" - Loading configuration");
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (MainConfiguration) configLoader.load("config.json", MainConfiguration.class);
        if (this.config == null) {
            this.logger.fatal("Something went wrong while trying to load the configuration, exiting program...");
            System.exit(0);
        }

        logger.info(" - Registering and connecting to database");
        database = new DatabaseManager(this);

        logger.info(" - Registering database table migrations");
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

        this.registerCommands();
        this.registerJobs();

        try {
            logger.info(" - Creating bot instance and connecting to Discord network");
            jda = prepareJDA().buildAsync();
        } catch (LoginException | RateLimitedException ex) {
            this.logger.fatal("Something went wrong while trying to connect to Discord, exiting program...");
            this.logger.fatal(ex);
            System.exit(0);
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public String getVersion() {
        return properties.getProperty("version");
    }

    private void registerCommands() {
        logger.info(" - Registering commands...");

        // Administration
        CommandHandler.register(new AddSelfAssignableRoleCommand(this));
        CommandHandler.register(new AliasCommand(this));
        CommandHandler.register(new AutoAssignRoleCommand(this));
        CommandHandler.register(new BanCommand(this));
        CommandHandler.register(new ChangePrefixCommand(this));
        CommandHandler.register(new ChannelIdCommand(this));
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
        CommandHandler.register(new SoftBanCommand(this));
        CommandHandler.register(new UserIdCommand(this));
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
        CommandHandler.register(new GlobalLeaderboardCommand(this));
        CommandHandler.register(new PingCommand(this));
        CommandHandler.register(new LeaderboardCommand(this));
        CommandHandler.register(new RankCommand(this));
        CommandHandler.register(new InviteCommand(this));
        CommandHandler.register(new SourceCommand(this));
        CommandHandler.register(new StatsCommand(this));

        logger.info(String.format(" - Registered %s commands successfully!", CommandHandler.getCommands().size()));
    }

    private void registerJobs() {
        logger.info(" - Registering jobs...");

        ScheduleHandler.registerJob(new ChangeGameJob(this));
        ScheduleHandler.registerJob(new GithubChangesJob(this));
        ScheduleHandler.registerJob(new FetchMemeTypesJob(this));
        ScheduleHandler.registerJob(new GarbageCollectorJob(this));
        ScheduleHandler.registerJob(new UpdateAudioPlayedTimeJob(this));
        ScheduleHandler.registerJob(new ResetRespectStatisticsJob(this));

        logger.info(String.format(" - Registered %s jobs successfully!", ScheduleHandler.entrySet().size()));
    }

    private JDABuilder prepareJDA() {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(this.config.botAuth().getToken());

        Class[] eventArguments = new Class[1];
        eventArguments[0] = Orion.class;

        for (EventTypes event : EventTypes.values()) {
            try {
                Object instance = event.getInstance().getDeclaredConstructor(eventArguments).newInstance(this);

                if (instance instanceof EventHandler) {
                    builder.addEventListener(instance);
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                this.logger.fatal("Invalid listener adapter object parsed, failed to create a new instance!");
                this.logger.fatal(ex);
            } catch (IllegalAccessException ex) {
                this.logger.fatal("An attempt was made to register a event listener called " + event + " but it failed somewhere!");
                this.logger.fatal(ex);
            }
        }

        return builder.setReconnectQueue(new SessionReconnectQueue()).setAutoReconnect(true);
    }
}
