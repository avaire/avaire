package com.avairebot.metrics.routes;

import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class GetStats extends SparkRoute {

    public GetStats(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONObject root = new JSONObject();

        root.put("application", buildApplication());
        root.put("shards", buildShards());
        root.put("global", buildGlobal());

        return root;
    }

    private JSONObject buildApplication() {
        JSONObject app = new JSONObject();

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        app.put("uptime", runtime.getUptime());
        app.put("startTime", runtime.getStartTime());
        app.put("memoryTotal", Runtime.getRuntime().totalMemory());
        app.put("memoryFree", Runtime.getRuntime().freeMemory());
        app.put("memoryMax", Runtime.getRuntime().maxMemory());
        app.put("availableProcessors", Runtime.getRuntime().availableProcessors());

        return app;
    }

    private JSONArray buildShards() {
        JSONArray shards = new JSONArray();

        for (JDA shard : metrics.getAvaire().getShardManager().getShards()) {
            JSONObject stats = new JSONObject();
            stats.put("id", shard.getShardInfo().getShardId())
                .put("guilds", shard.getGuilds().size())
                .put("users", shard.getUsers().size())
                .put("status", shard.getStatus())
                .put("channels", (shard.getTextChannels().size() + shard.getVoiceChannels().size()))
                .put("latency", shard.getPing());

            shards.put(stats);
        }

        return shards;
    }

    private JSONObject buildGlobal() {
        JSONObject global = new JSONObject();
        global.put("guilds", metrics.getAvaire().getShardEntityCounter().getGuilds());
        global.put("users", metrics.getAvaire().getShardEntityCounter().getUsers());

        JSONObject channels = new JSONObject();
        channels.put("total", metrics.getAvaire().getShardEntityCounter().getChannels());
        channels.put("text", metrics.getAvaire().getShardEntityCounter().getTextChannels());
        channels.put("voice", metrics.getAvaire().getShardEntityCounter().getVoiceChannels());

        global.put("channels", channels);

        return global;
    }
}
