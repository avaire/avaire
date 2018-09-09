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

package com.avairebot.database.serializer;

import com.avairebot.database.transformers.PlaylistTransformer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class PlaylistSongSerializer implements JsonSerializer<PlaylistTransformer.PlaylistSong> {

    @Override
    public JsonElement serialize(PlaylistTransformer.PlaylistSong src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject song = new JsonObject();

        song.addProperty("title", src.getTitle().replaceAll("\"", "\\\\\""));
        song.addProperty("duration", src.getDuration());
        song.addProperty("link", src.getLink());

        return song;
    }
}
