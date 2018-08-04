package com.avairebot.metrics.routes;

import com.avairebot.Constants;
import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.database.collection.DataRow;
import com.avairebot.metrics.Metrics;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashSet;
import java.util.Set;

public class GetGuildCleanup extends SparkRoute {

    private static final Logger log = LoggerFactory.getLogger(GetGuildCleanup.class);

    public GetGuildCleanup(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!hasValidAuthorizationHeader(request)) {
            log.warn("Unauthorized request, missing or invalid \"Authorization\" header give.");
            return buildResponse(response, 401, "Unauthorized request, missing or invalid \"Authorization\" header give.");
        }

        Set<String> missingGuilds = new HashSet<>();

        for (DataRow row : metrics.getAvaire().getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME).select("id").get()) {
            if (metrics.getAvaire().getShardManager().getGuildById(row.getString("id")) == null) {
                missingGuilds.add(row.getString("id"));
            }
        }

        JSONObject root = new JSONObject();
        root.put("ids", missingGuilds);

        return root;
    }

    private boolean hasValidAuthorizationHeader(Request request) {
        String authorization = request.headers("Authorization");

        return authorization != null && authorization.equals(getAuthorizationToken());
    }

    private String getAuthorizationToken() {
        return metrics.getAvaire().getConfig().getString("metrics.authToken", "avaire-auth-token");
    }
}
