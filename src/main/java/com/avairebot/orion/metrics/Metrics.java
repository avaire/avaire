package com.avairebot.orion.metrics;

import ch.qos.logback.classic.LoggerContext;
import com.avairebot.orion.Orion;
import com.avairebot.orion.metrics.handlers.SparkExceptionHandler;
import com.avairebot.orion.metrics.routes.GetMetrics;
import com.avairebot.orion.metrics.routes.GetStats;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.logback.InstrumentedAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class Metrics {

    // ################################################################################
    // ##                              JDA Stats
    // ################################################################################

    public static final Counter jdaEvents = Counter.build()
        .name("orion_jda_events_received_total")
        .help("All events that JDA provides us with by class")
        .labelNames("class") // GuildJoinedEvent, MessageReceivedEvent, ReconnectEvent etc
        .register();

    // ################################################################################
    // ##                          Orion Stats
    // ################################################################################

    public static final Counter commandsRatelimited = Counter.build()
        .name("orion_commands_ratelimited_total")
        .help("Total ratelimited commands")
        .labelNames("class") // use the simple name of the command class
        .register();

    // music stuff

    public static final Counter searchRequests = Counter.build() //search requests issued by users
        .name("orion_music_search_requests_total")
        .help("Total search requests")
        .register();

    public static final Counter tracksLoaded = Counter.build()
        .name("orion_music_tracks_loaded_total")
        .help("Total tracks loaded by the audio loader")
        .register();

    public static final Counter trackLoadsFailed = Counter.build()
        .name("orion_music_track_loads_failed_total")
        .help("Total failed track loads by the audio loader")
        .register();

    // commands

    public static final Counter commandsReceived = Counter.build()
        .name("orion_commands_received_total")
        .help("Total received commands. Some of these might get ratelimited.")
        .labelNames("class") // use the simple name of the command class: PlayCommand, DanceCommand, ShardsCommand etc
        .register();

    public static final Counter commandsExecuted = Counter.build()
        .name("orion_commands_executed_total")
        .help("Total executed commands by class")
        .labelNames("class") // use the simple name of the command class: PlayCommand, DanceCommand, ShardsCommand etc
        .register();

    public static final Histogram executionTime = Histogram.build() // commands execution time, excluding ratelimited ones
        .name("orion_command_execution_duration_seconds")
        .help("Command execution time, excluding handling ratelimited commands.")
        .labelNames("class") // use the simple name of the command class: PlayCommand, DanceCommand, ShardsCommand etc
        .register();

    public static final Counter commandExceptions = Counter.build()
        .name("orion_commands_exceptions_total")
        .help("Total uncaught exceptions thrown by command invocation")
        .labelNames("class") // class of the exception
        .register();

    // ################################################################################
    // ##                           Method Stuff
    // ################################################################################

    public static final Logger LOGGER = LoggerFactory.getLogger(Metrics.class);

    private static final int PORT = 1256;

    private final Orion orion;

    public Metrics(Orion orion) {
        this.orion = orion;

        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
        final InstrumentedAppender prometheusAppender = new InstrumentedAppender();
        prometheusAppender.setContext(root.getLoggerContext());
        prometheusAppender.start();
        root.addAppender(prometheusAppender);

        // JVM (hotspot) metrics
        DefaultExports.initialize();

        LOGGER.info("Igniting Spark API on port: " + PORT);

        Spark.port(PORT);

        Spark.before(new HttpFilter());
        Spark.exception(Exception.class, new SparkExceptionHandler());

        Spark.get("/metrics", new GetMetrics(this));
        Spark.get("/stats", new GetStats(this));
    }

    public Orion getOrion() {
        return orion;
    }
}
