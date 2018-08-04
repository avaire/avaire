package com.avairebot.metrics;

import ch.qos.logback.classic.LoggerContext;
import com.avairebot.AvaIre;
import com.avairebot.commands.Category;
import com.avairebot.commands.utility.GlobalLeaderboardCommand;
import com.avairebot.commands.utility.LeaderboardCommand;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.controllers.PlaylistController;
import com.avairebot.handlers.adapter.JDAStateEventAdapter;
import com.avairebot.metrics.filters.AreWeReadyYetFilter;
import com.avairebot.metrics.filters.HttpFilter;
import com.avairebot.metrics.handlers.SparkExceptionHandler;
import com.avairebot.metrics.routes.*;
import com.avairebot.middleware.ThrottleMiddleware;
import com.avairebot.utilities.LevelUtil;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.logback.InstrumentedAppender;
import net.dv8tion.jda.core.events.Event;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.lang.reflect.Modifier;
import java.util.Set;

public class Metrics {

    // ################################################################################
    // ##                              JDA Stats
    // ################################################################################

    public static final Counter jdaEvents = Counter.build()
        .name("avaire_jda_events_received_total")
        .help("All events that JDA provides us with by class")
        .labelNames("class") // GuildJoinedEvent, MessageReceivedEvent, ReconnectEvent etc
        .register();


    public static final Gauge memoryTotal = Gauge.build()
        .name("avaire_memory_total")
        .help("Total number bytes of memory dedicated to the app")
        .register();

    public static final Gauge memoryUsed = Gauge.build()
        .name("avaire_memory_used")
        .help("Total number bytes used in memory for the app")
        .register();

    // ################################################################################
    // ##                             AvaIre Stats
    // ################################################################################

    public static final Gauge guilds = Gauge.build()
        .name("avaire_guilds_total")
        .help("Total number of guilds the bot is in")
        .register();

    public static final Gauge geoTracker = Gauge.build()
        .name("avaire_geo_tracker_total")
        .help("Total number of guilds split up by geographic location")
        .labelNames("region")
        .register();

    public static final Gauge websocketHeartbeat = Gauge.build()
        .name("avaire_shard_websocket_heartbeat")
        .help("Websocket heartbeat in milliseconds for each shard")
        .labelNames("shard")
        .register();

    // Music

    public static final Counter searchRequests = Counter.build() //search requests issued by users
        .name("avaire_music_search_requests_total")
        .help("Total search requests")
        .register();

    public static final Counter tracksLoaded = Counter.build()
        .name("avaire_music_tracks_loaded_total")
        .help("Total tracks loaded by the audio loader")
        .register();

    public static final Counter trackLoadsFailed = Counter.build()
        .name("avaire_music_track_loads_failed_total")
        .help("Total failed track loads by the audio loader")
        .register();

    public static final Gauge musicPlaying = Gauge.build()
        .name("avaire_guild_music_playing_total")
        .help("Total number of guilds listening to music")
        .register();

    // Commands

    public static final Counter commandsRatelimited = Counter.build()
        .name("avaire_commands_ratelimited_total")
        .help("Total ratelimited commands")
        .labelNames("class") // use the simple name of the command class
        .register();

    public static final Counter slowmodeRatelimited = Counter.build()
        .name("avaire_slowmode_ratelimited_total")
        .help("Total ratelimited messages")
        .labelNames("channel")
        .register();

    public static final Counter commandsReceived = Counter.build()
        .name("avaire_commands_received_total")
        .help("Total received commands. Some of these might get ratelimited.")
        .labelNames("class")
        .register();

    public static final Counter commandsExecuted = Counter.build()
        .name("avaire_commands_executed_total")
        .help("Total executed commands by class")
        .labelNames("class")
        .register();

    public static final Histogram executionTime = Histogram.build() // commands execution time, excluding ratelimited ones
        .name("avaire_command_execution_duration_seconds")
        .help("Command execution time, excluding handling ratelimited commands.")
        .labelNames("class")
        .register();

    public static final Counter commandExceptions = Counter.build()
        .name("avaire_commands_exceptions_total")
        .help("Total uncaught exceptions thrown by command invocation")
        .labelNames("class") // class of the exception
        .register();

    // AI Requests

    public static final Counter aiRequestsReceived = Counter.build()
        .name("avaire_ai_received_total")
        .help("Total received ai requests.")
        .register();

    public static final Counter aiRequestsExecuted = Counter.build()
        .name("avaire_ai_executed_total")
        .help("Total executed ai intents by class")
        .labelNames("class")
        .register();

    public static final Histogram aiExecutionTime = Histogram.build()
        .name("avaire_ai_execution_duration_seconds")
        .help("AI intent execution time.")
        .labelNames("class")
        .register();

    // Database requests

    public static final Counter databaseQueries = Counter.build()
        .name("avaire_database_queries")
        .help("Total prepared statements created for the given type")
        .labelNames("type")
        .register();

    // ################################################################################
    // ##                           Method Stuff
    // ################################################################################

    public static final Logger log = LoggerFactory.getLogger(Metrics.class);

    private static int port = 1256;

    private static AvaIre avaire;
    private static boolean isSetup = false;

    public static void setup(AvaIre avaire) {
        if (isSetup) {
            throw new IllegalStateException("The metrics has already been setup!");
        }

        Metrics.avaire = avaire;

        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
        final InstrumentedAppender prometheusAppender = new InstrumentedAppender();
        prometheusAppender.setContext(root.getLoggerContext());
        prometheusAppender.start();
        root.addAppender(prometheusAppender);

        // JVM (hotspot) metrics
        DefaultExports.initialize();
        Metrics.initializeEventMetrics();

        CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();
        cacheMetrics.addCache("levels", LevelUtil.cache);
        cacheMetrics.addCache("guilds", GuildController.cache);
        cacheMetrics.addCache("players", PlayerController.cache);
        cacheMetrics.addCache("playlists", PlaylistController.cache);
        cacheMetrics.addCache("categoryPrefixes", Category.cache);
        cacheMetrics.addCache("throttleCommands", ThrottleMiddleware.cache);
        cacheMetrics.addCache("throttleMessages", ThrottleMiddleware.messageCache);
        cacheMetrics.addCache("autorole", JDAStateEventAdapter.cache);
        cacheMetrics.addCache("leaderboard", LeaderboardCommand.cache);
        cacheMetrics.addCache("global-leaderboard", GlobalLeaderboardCommand.cache);

        if (!avaire.getConfig().getBoolean("metrics.enabled", true)) {
            log.info("Metrics web API is disabled, skipping igniting Spark API");
            Metrics.isSetup = true;
            return;
        }

        port = avaire.getConfig().getInt("metrics.port", 1256);

        log.info("Igniting Spark API on port: " + port);

        Spark.port(port);

        Spark.notFound(new GetNotFoundRoute(MetricsHolder.METRICS));
        Spark.exception(Exception.class, new SparkExceptionHandler());

        Spark.before(new HttpFilter());
        Spark.before(new AreWeReadyYetFilter(avaire));

        Spark.get("/players/cleanup", new GetPlayerCleanup(MetricsHolder.METRICS));
        Spark.get("/guilds/cleanup", new GetGuildCleanup(MetricsHolder.METRICS));
        Spark.get("/guilds/:ids/exists", new GetGuildsExists(MetricsHolder.METRICS));
        Spark.get("/guilds/:ids", new GetGuilds(MetricsHolder.METRICS));
        Spark.get("/metrics", new GetMetrics(MetricsHolder.METRICS));
        Spark.get("/stats", new GetStats(MetricsHolder.METRICS));
        Spark.post("/vote", new PostVote(MetricsHolder.METRICS));

        Metrics.isSetup = true;
    }

    private static void initializeEventMetrics() {
        Set<Class<? extends Event>> types = new Reflections("net.dv8tion.jda.core.events")
            .getSubTypesOf(Event.class);

        for (Class<? extends Event> type : types) {
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            Metrics.jdaEvents.labels(type.getSimpleName()).inc(0D);
        }
    }

    public AvaIre getAvaire() {
        return avaire;
    }

    private static class MetricsHolder {
        private static final Metrics METRICS = new Metrics();
    }
}
