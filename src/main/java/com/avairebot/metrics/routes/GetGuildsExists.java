package com.avairebot.metrics.routes;

import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class GetGuildsExists extends SparkRoute {

    public GetGuildsExists(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String[] ids = request.params("ids").split(",");

        JSONObject root = new JSONObject();
        for (String id : ids) {
            try {
                Guild guildById = metrics.getAvaire().getShardManager().getGuildById(Long.parseLong(id));
                if (guildById == null) {
                    root.put(id, false);
                    continue;
                }
                root.put(id, true);
            } catch (NumberFormatException e) {
                root.put(id, false);
            }
        }

        return root;
    }
}
