package com.avairebot.orion.requests.service;

import java.util.List;
import java.util.Map;

public class GfycatService {
    private List<Map<String, Object>> gfycats;
    private int found;
    private String cursor;

    public List<Map<String, Object>> getGfycats() {
        return gfycats;
    }

    public int getFound() {
        return found;
    }

    public String getCursor() {
        return cursor;
    }
}
