package com.avairebot.orion.requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Request {
    private final String url;
    private final RequestType type;

    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    private Consumer<Throwable> DEFAULT_FAILURE = (t) -> {
    };
    private Consumer<Response> DEFAULT_SUCCESS = (Response) -> {
    };

    public Request(String url) {
        this(url, RequestType.GET);
    }

    public Request(String url, RequestType type) {
        this.url = url;
        this.type = type;

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

    public void send() {
        this.send(null, null);
    }

    public void send(Consumer success) {
        this.send(success, null);
    }

    public void send(final Consumer<Response> success, final Consumer<Throwable> failure) {
        new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) buildUrl().openConnection();

                con.setRequestMethod(type.name());

                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }

                String inputLine;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer buffer = new StringBuffer();

                while ((inputLine = bufferedReader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                bufferedReader.close();

                Response response = new Response(buffer.toString());
                if (success != null) {
                    success.accept(response);
                } else {
                    DEFAULT_SUCCESS.accept(response);
                }
            } catch (Exception ex) {
                if (failure != null) {
                    failure.accept(ex);
                } else {
                    DEFAULT_FAILURE.accept(ex);
                }
            }
        }).start();
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
