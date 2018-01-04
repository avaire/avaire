package com.avairebot.metrics.handlers;

import com.avairebot.metrics.Metrics;
import org.apache.commons.lang3.exception.ExceptionUtils;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

public class SparkExceptionHandler implements ExceptionHandler {

    @Override
    public void handle(Exception exception, Request request, Response response) {
        Metrics.LOGGER.error(request.requestMethod() + " " + request.pathInfo(), exception);

        response.body(ExceptionUtils.getStackTrace(exception));
        response.type("text/plain");
        response.status(500);
    }
}
