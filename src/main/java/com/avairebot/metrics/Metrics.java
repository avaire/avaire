/*
 * Copyright (c) 2019.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.metrics;

import ch.qos.logback.classic.LoggerContext;
import com.avairebot.AvaIre;
import com.avairebot.blacklist.Ratelimit;
import com.avairebot.commands.Category;
import com.avairebot.commands.utility.GlobalLeaderboardCommand;
import com.avairebot.commands.utility.LeaderboardCommand;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.commands.InteractionCommand;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.controllers.*;
import com.avairebot.handlers.adapter.JDAStateEventAdapter;
import com.avairebot.level.LevelManager;
import com.avairebot.metrics.routes.GetMetrics;
import com.avairebot.middleware.ThrottleMiddleware;
import com.avairebot.scheduler.jobs.LavalinkGarbageNodeCollectorJob;
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

    public static final Gauge uptime = Gauge.build()
        .name("avaire_uptime")
        .help("Total number of seconds the bot has been online for")
        .labelNames("type")
        .register();

    public static final Gauge guilds = Gauge.build()
        .name("avaire_guilds_total")
        .help("Total number of guilds the bot is in")
        .register();

    public static final Gauge channels = Gauge.build()
        .name("avaire_channels_total")
        .help("Total number of channels the bot is in")
        .labelNames("type")
        .register();

    public static final Gauge users = Gauge.build()
        .name("avaire_users_total")
        .help("Total number of users the bot is in")
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

    public static final Counter commandAttempts = Counter.build()
        .name("avaire_command_attempts_total")
        .help("Total amount of command attempts by class")
        .labelNames("class")
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

    // Vote statistics

    public static final Counter dblVotes = Counter.build()
        .name("avaire_dbl_votes")
        .help("Vote requests through the webhook vs the command check")
        .labelNames("type")
        .register();

    public static final Gauge validVotes = Gauge.build()
        .name("avaire_total_valid_votes")
        .help("The amount of valid votes currently active, updated once every minute")
        .register();

    // Blacklist

    public static final Gauge blacklist = Gauge.build()
        .name("avaire_blacklist_current")
        .help("The amount of servers and users that are currently on the blacklist")
        .labelNames("type")
        .register();

    // ################################################################################
    // ##                           Method Stuff
    // ################################################################################

    public static final Logger log = LoggerFactory.getLogger(Metrics.class);

    private static boolean isSetup = false;

    public static void setup(AvaIre avaire) {
        if (isSetup) {
            throw new IllegalStateException("The metrics has already been setup!");
        }

        uptime.labels("static").set(System.currentTimeMillis());

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
        cacheMetrics.addCache("levels", LevelManager.cache);
        cacheMetrics.addCache("guilds", GuildController.cache);
        cacheMetrics.addCache("players", PlayerController.cache);
        cacheMetrics.addCache("purchases", PurchaseController.cache);
        cacheMetrics.addCache("playlists", PlaylistController.cache);
        cacheMetrics.addCache("categoryPrefixes", Category.cache);
        cacheMetrics.addCache("reaction-roles", ReactionController.cache);
        cacheMetrics.addCache("throttleCommands", ThrottleMiddleware.cache);
        cacheMetrics.addCache("middlewareThrottleMessages", Middleware.messageCache);
        cacheMetrics.addCache("autorole", JDAStateEventAdapter.cache);
        cacheMetrics.addCache("rankScores", RankCommand.cache);
        cacheMetrics.addCache("leaderboard", LeaderboardCommand.cache);
        cacheMetrics.addCache("global-leaderboard", GlobalLeaderboardCommand.cache);
        cacheMetrics.addCache("interaction-lottery", InteractionCommand.cache);
        cacheMetrics.addCache("blacklist-ratelimit", Ratelimit.cache);
        cacheMetrics.addCache("lavalink-destroy-cleanup", LavalinkGarbageNodeCollectorJob.cache);

        if (!avaire.getConfig().getBoolean("web-servlet.metrics",
            avaire.getConfig().getBoolean("metrics.enabled", true)
        )) {
            log.info("Metrics web API is disabled, skipping igniting Spark API");
            isSetup = true;
            return;
        }

        avaire.getServlet().registerGet("/metrics", new GetMetrics());

        isSetup = true;
    }

    private static void initializeEventMetrics() {
        Set<Class<? extends Event>> types = new Reflections("net.dv8tion.jda.core.events")
            .getSubTypesOf(Event.class);

        for (Class<? extends Event> type : types) {
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            jdaEvents.labels(type.getSimpleName()).inc(0D);
        }
    }
}
