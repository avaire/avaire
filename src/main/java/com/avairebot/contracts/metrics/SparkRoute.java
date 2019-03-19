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

import com.avairebot.AvaIre;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class SparkRoute implements Route {

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

    /**
     * Checks if the incoming request has a valid Authorization header with a valid auth
     * token, the token is validated by whatever is returned by
     * the {@link #getAuthorizationToken()} method.
     *
     * @param request The spark request instance for the incoming request.
     * @return {@code True} if the given request instance has a valid auth header, {@code False} otherwise.
     */
    protected boolean hasValidAuthorizationHeader(Request request) {
        String authorization = request.headers("Authorization");

        return authorization != null && authorization.equals(getAuthorizationToken());
    }

    /**
     * Gets the authorization token that should be used
     * to verify if a request is valid or not.
     *
     * @return The string representing the auth token.
     */
    @SuppressWarnings("WeakerAccess")
    protected String getAuthorizationToken() {
        return AvaIre.getInstance().getConfig().getString("web-servlet.authToken",
            AvaIre.getInstance().getConfig().getString("metrics.authToken", "avaire-auth-token")
        );
    }
}
