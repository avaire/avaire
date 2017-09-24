package com.avairebot.orion.database.transformers;

import com.avairebot.orion.contracts.database.transformers.Transformer;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.time.Carbon;

public class GuildTransformer extends Transformer {

    private boolean levels = false;

    public GuildTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            levels = data.getBoolean("levels");
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
        return data.getBoolean("level_alerts");
    }

    public String getLevelChannel() {
        return data.getString("level_channel");
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
