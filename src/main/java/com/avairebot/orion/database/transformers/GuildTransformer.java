package com.avairebot.orion.database.transformers;

import com.avairebot.orion.contracts.database.transformers.Transformer;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.time.Carbon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class GuildTransformer extends Transformer {

    private final Map<String, String> prefixes = new HashMap<>();

    private boolean levels = false;
    private boolean levelAlerts = false;
    private String levelChannel = null;
    private String autorole = null;

    public GuildTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            levels = data.getBoolean("levels");
            levelAlerts = data.getBoolean("level_alerts");
            levelChannel = data.getString("level_channel");
            autorole = data.getString("autorole");

            if (data.getString("prefixes", null) != null) {
                HashMap<String, String> dbPrefixes = new Gson().fromJson(
                        data.getString("prefixes"),
                        new TypeToken<HashMap<String, String>>() {
                        }.getType());

                for (Map.Entry<String, String> item : dbPrefixes.entrySet()) {
                    prefixes.put(item.getKey().toLowerCase(), item.getValue());
                }
            }
        }
    }

    public String getId() {
        return data.getString("id");
    }

    public long getLongId() {
        return data.getLong("id");
    }

    public String getOwnerId() {
        return data.getString("owner");
    }

    public long getOwnerLongId() {
        return data.getLong("owner");
    }

    public String getName() {
        return data.getString("name");
    }

    public String getIcon() {
        return data.getString("icon");
    }

    public String getLocal() {
        return data.getString("local");
    }

    public boolean isLevels() {
        return levels;
    }

    public void setLevels(boolean level) {
        levels = level;
    }

    public boolean isLevelAlerts() {
        return levelAlerts;
    }

    public void setLevelAlerts(boolean levelAlerts) {
        this.levelAlerts = levelAlerts;
    }

    public String getLevelChannel() {
        return levelChannel;
    }

    public void setLevelChannel(String levelChannel) {
        this.levelChannel = levelChannel;
    }

    public String getAutorole() {
        return autorole;
    }

    public void setAutorole(String autorole) {
        this.autorole = autorole;
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public Carbon getCreatedAt() {
        return data.getTimestamp("created_at");
    }

    public Carbon getUpdatedAt() {
        return data.getTimestamp("updated_at");
    }

    public Carbon getLeftGuildAt() {
        return data.getTimestamp("leftguild_at");
    }
}
