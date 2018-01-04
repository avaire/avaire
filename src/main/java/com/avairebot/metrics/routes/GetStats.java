package com.avairebot.metrics.routes;

import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import com.avairebot.shard.AvaireShard;
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

        for (AvaireShard shard : metrics.getAvaire().getShards()) {
            JSONObject fbStats = new JSONObject();
            fbStats.put("id", shard.getShardId())
                .put("guilds", shard.getJDA().getGuilds().size())
                .put("users", shard.getJDA().getUsers().size())
                .put("status", shard.getJDA().getStatus());

            a.put(fbStats);
        }

        JSONObject g = new JSONObject();
        g.put("guilds", metrics.getAvaire().getShardEntityCounter().getGuilds());
        g.put("channels", metrics.getAvaire().getShardEntityCounter().getChannels());
        g.put("users", metrics.getAvaire().getShardEntityCounter().getUsers());

        root.put("shards", a);
        root.put("global", g);

        return root;
    }
}
