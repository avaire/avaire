package com.avairebot.orion.metrics;

import spark.Filter;
import spark.Request;
import spark.Response;

public class HttpFilter implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        Metrics.LOGGER.debug(request.requestMethod() + " " + request.pathInfo());

        response.header("Access-Control-Allow-Origin", "*");
        response.type("application/json");
    }
}
