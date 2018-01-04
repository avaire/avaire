package com.avairebot.requests;

import com.avairebot.contracts.async.Future;
import okhttp3.OkHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Request extends Future {

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
        return new URL(url + (url.contains("?") ? "" : '?') + buildUrlParameters());
    }

    private String buildUrlParameters() {
        return parameters.entrySet().stream().map(item -> {
            try {
                return String.format("%s=%s", item.getKey(), URLEncoder.encode(item.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return String.format("%s=%s", item.getKey(), "invalid-format");
            }
        }).collect(Collectors.joining("&"));
    }
}
