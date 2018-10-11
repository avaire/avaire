/*
 * Copyright (c) 2018.
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
