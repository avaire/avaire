package com.avairebot.orion.metrics.routes;

import com.avairebot.orion.contracts.metrics.SparkRoute;
import com.avairebot.orion.metrics.Metrics;
import com.avairebot.orion.metrics.PrometheusMetricsServlet;
import spark.Request;
import spark.Response;

public class GetMetrics extends SparkRoute {

    private static final PrometheusMetricsServlet metricsServlet = new PrometheusMetricsServlet();

    public GetMetrics(Metrics metrics) {
        super(metrics);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return metricsServlet.servletGet(request.raw(), response.raw());
    }
}
