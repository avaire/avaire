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
