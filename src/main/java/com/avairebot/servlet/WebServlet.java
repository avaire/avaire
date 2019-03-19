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

package com.avairebot.servlet;

import com.avairebot.servlet.filters.AreWeReadyYetFilter;
import com.avairebot.servlet.filters.HttpFilter;
import com.avairebot.servlet.handlers.SparkExceptionHandler;
import com.avairebot.servlet.routes.GetNotFoundRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;

public class WebServlet {

    public static final Logger log = LoggerFactory.getLogger(WebServlet.class);

    public static final int defaultPort = 1256;

    private final int port;
    private boolean initialized;

    public WebServlet(int port) {
        this.port = port;
        this.initialized = false;
    }

    private void initialize() {
        log.info("Igniting Spark API on port: " + port);

        Spark.port(port);

        Spark.notFound(new GetNotFoundRoute());
        Spark.exception(Exception.class, new SparkExceptionHandler());

        Spark.before(new HttpFilter());
        Spark.before(new AreWeReadyYetFilter());

        initialized = true;
    }

    /**
     * Map the route for HTTP GET requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerGet(final String path, final Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("GET {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.get(path, route);
    }

    /**
     * Map the route for HTTP POST requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerPost(final String path, final Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("POST {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.post(path, route);
    }

    /**
     * Map the route for HTTP PUT requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerPut(String path, Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("PUT {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.put(path, route);
    }

    /**
     * Map the route for HTTP PATCH requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerPatch(String path, Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("PATCH {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.patch(path, route);
    }

    /**
     * Map the route for HTTP DELETE requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerDelete(String path, Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("DELETE {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.delete(path, route);
    }

    /**
     * Map the route for HTTP HEAD requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerHead(String path, Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("HEAD {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.head(path, route);
    }

    /**
     * Map the route for HTTP TRACE requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerTrace(String path, Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("TRACE {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.trace(path, route);
    }

    /**
     * Map the route for HTTP OPTIONS requests
     *
     * @param path  the path
     * @param route The route
     */
    public synchronized void registerOptions(String path, Route route) {
        if (!initialized) {
            initialize();
        }

        log.debug("OPTIONS {} has been registered to {}", path, route.getClass().getTypeName());
        Spark.options(path, route);
    }
}
