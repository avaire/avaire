package com.avairebot.contracts.metrics;

import com.avairebot.metrics.Metrics;
import org.json.JSONObject;
import spark.Response;
import spark.Route;

public abstract class SparkRoute implements Route {

    protected final Metrics metrics;

    public SparkRoute(Metrics metrics) {
        this.metrics = metrics;
    }

    protected JSONObject buildResponse(Response response, int code, String message) {
        response.status(code);

        JSONObject root = new JSONObject();

        root.put("status", code);
        root.put(code == 200 ? "message" : "reason", message);

        return root;
    }
}
