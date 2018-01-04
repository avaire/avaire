package com.avairebot.metrics;

import io.prometheus.client.exporter.MetricsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PrometheusMetricsServlet extends MetricsServlet {

    public HttpServletResponse servletGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
        return resp;
    }
}
