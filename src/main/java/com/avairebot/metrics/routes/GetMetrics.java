/*
 * Copyright (c) 2019.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.metrics.routes;

import com.avairebot.contracts.metrics.SparkRoute;
import com.avairebot.metrics.PrometheusMetricsServlet;
import spark.Request;
import spark.Response;

public class GetMetrics extends SparkRoute {

    private static final PrometheusMetricsServlet metricsServlet = new PrometheusMetricsServlet();

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return metricsServlet.servletGet(request.raw(), response.raw());
    }
}
