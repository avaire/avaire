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
        JSONObject root = new JSONObject();
        JSONArray shards = new JSONArray();

        for (AvaireShard shard : metrics.getAvaire().getShards()) {
            JSONObject stats = new JSONObject();
            stats.put("id", shard.getShardId())
                .put("guilds", shard.getJDA().getGuilds().size())
                .put("users", shard.getJDA().getUsers().size())
                .put("status", shard.getJDA().getStatus())
                .put("channels", (shard.getJDA().getTextChannels().size() + shard.getJDA().getVoiceChannels().size()));

            shards.put(stats);
        }

        JSONObject global = new JSONObject();
        global.put("guilds", metrics.getAvaire().getShardEntityCounter().getGuilds());
        global.put("channels", metrics.getAvaire().getShardEntityCounter().getChannels());
        global.put("users", metrics.getAvaire().getShardEntityCounter().getUsers());

        root.put("shards", shards);
        root.put("global", global);

        return root;
    }
}
