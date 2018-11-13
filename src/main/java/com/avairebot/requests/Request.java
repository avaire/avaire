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

package com.avairebot.requests;

import com.avairebot.contracts.async.Future;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Request extends Future {

    private static final Logger log = LoggerFactory.getLogger(Request.class);

    private final String url;
    private final RequestType type;

    private final OkHttpClient client;
    private final okhttp3.Request.Builder builder;

    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    public Request(String url) {
        this(url, RequestType.GET);
    }

    public Request(String url, RequestType type) {
        this.url = url;
        this.type = type;

        client = new OkHttpClient();
        builder = new okhttp3.Request.Builder();
        headers.put("User-Agent", "Mozilla/5.0");
    }

    public Request addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Request addParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    protected void handle(Consumer success, Consumer<Throwable> failure) {
        try {
            builder.url(buildUrl());

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }

            switch (type) {
                case GET:
                    builder.get();
                    break;
            }

            success.accept(new Response(client.newCall(builder.build()).execute()));
        } catch (Exception ex) {
            failure.accept(ex);
        }
    }

    private URL buildUrl() throws MalformedURLException {
        String builtUrl = url + (url.contains("?") ? "" : '?') + buildUrlParameters();

        log.debug("Building URL request: {} {}", type.name(), builtUrl);

        return new URL(builtUrl);
    }

    private String buildUrlParameters() {
        return parameters.entrySet().stream().map(item -> {
            try {
                return String.format("%s=%s", item.getKey(), URLEncoder.encode(
                    item.getValue().toString(), "UTF-8"
                ));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return String.format("%s=%s", item.getKey(), "invalid-format");
            }
        }).collect(Collectors.joining("&"));
    }
}
