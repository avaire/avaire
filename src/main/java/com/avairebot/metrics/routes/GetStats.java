package com.avairebot.metrics.routes;

import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;
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

        for (JDA shard : metrics.getAvaire().getShardManager().getShards()) {
            JSONObject stats = new JSONObject();
            stats.put("id", shard.getShardInfo().getShardId())
                .put("guilds", shard.getGuilds().size())
                .put("users", shard.getUsers().size())
                .put("status", shard.getStatus())
                .put("channels", (shard.getTextChannels().size() + shard.getVoiceChannels().size()));

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
