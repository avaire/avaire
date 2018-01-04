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
