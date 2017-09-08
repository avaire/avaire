package com.avairebot.orion.requests;

import com.google.gson.Gson;

public class Response {
    private final String output;

    public Response(String output) {
        this.output = output;
    }

    public Object toJson(Class<?> clazz) {
        Gson gson = new Gson();

        return gson.fromJson(toString(), clazz);
    }

    public String toString() {
        return output;
    }
}
