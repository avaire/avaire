package com.avairebot.metrics.routes;

import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.Metrics;
import spark.Request;
import spark.Response;

public class GetNotFoundRoute extends SparkRoute {

    public GetNotFoundRoute(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return "{\"status\": 404, \"reason\": \"Requested route does not exists.\"}";
    }
}
