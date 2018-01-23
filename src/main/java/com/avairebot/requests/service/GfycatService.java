package com.avairebot.requests.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GfycatService {

    private List<Map<String, Object>> gfycats;
    private int found;
    private String cursor;

    public List<Map<String, Object>> getGfycats() {
        return gfycats;
    }

    public Map<String, Object> getRandomGfycatsItem() {
        if (getGfycats() == null || getGfycats().isEmpty()) {
            return null;
        }

        ArrayList<Map<String, Object>> items = new ArrayList<>(getGfycats());
        Collections.shuffle(items);

        return items.get(0);
    }

    public int getFound() {
        return found;
    }

    public String getCursor() {
        return cursor;
    }
}
