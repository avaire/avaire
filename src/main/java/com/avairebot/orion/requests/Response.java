package com.avairebot.orion.requests;

import com.avairebot.orion.Orion;

import java.io.IOException;

public class Response {
    private final okhttp3.Response response;

    public Response(okhttp3.Response response) {
        this.response = response;
    }

    public okhttp3.Response getResponse() {
        return response;
    }

    public Object toService(Class<?> clazz) {
        return Orion.GSON.fromJson(toString(), clazz);
    }

    @Override
    public String toString() {
        try {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
