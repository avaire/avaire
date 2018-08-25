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


    /**
     * Builds the JSON response, generating a "status" key, along with a "message" key
     * for 200 successful responses, or a "reason" key for other response codes.
     *
     * @param response The spark response for the requests.
     * @param code     The status code for the response.
     * @param message  The message or reason for the responses.
     * @return Builds the spark status responses object.
     */
    protected JSONObject buildResponse(Response response, int code, String message) {
        response.status(code);

        JSONObject root = new JSONObject();

        root.put("status", code);
        root.put(code == 200 ? "message" : "reason", message);

        return root;
    }
}
