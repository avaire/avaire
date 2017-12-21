package com.avairebot.orion.metrics.routes;

import com.avairebot.orion.contracts.metrics.SparkRoute;
import com.avairebot.orion.metrics.Metrics;
import com.avairebot.orion.shard.OrionShard;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class GetStats extends SparkRoute {

    public GetStats(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        JSONObject root = new JSONObject();
        JSONArray a = new JSONArray();

        for (OrionShard shard : metrics.getOrion().getShards()) {
            JSONObject fbStats = new JSONObject();
            fbStats.put("id", shard.getShardId())
                .put("guilds", shard.getJDA().getGuilds().size())
                .put("users", shard.getJDA().getUsers().size())
                .put("status", shard.getJDA().getStatus());

            a.put(fbStats);
        }

        JSONObject g = new JSONObject();
        g.put("guilds", metrics.getOrion().getShardEntityCounter().getGuilds());
        g.put("channels", metrics.getOrion().getShardEntityCounter().getChannels());
        g.put("users", metrics.getOrion().getShardEntityCounter().getUsers());

        root.put("shards", a);
        root.put("global", g);

        return root;
    }
}
