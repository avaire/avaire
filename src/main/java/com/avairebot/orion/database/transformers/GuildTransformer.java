package com.avairebot.orion.database.transformers;

import com.avairebot.orion.contracts.database.transformers.Transformer;
import com.avairebot.orion.database.collection.DataRow;
import com.avairebot.orion.time.Carbon;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;

public class GuildTransformer extends Transformer {

    private final Map<String, String> aliases = new HashMap<>();
    private final Map<String, String> prefixes = new HashMap<>();
    private final Map<String, String> selfAssignableRoles = new HashMap<>();
    private final List<ChannelTransformer> channels = new ArrayList<>();

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

            if (data.getString("aliases", null) != null) {
                HashMap<String, String> dbAliases = new Gson().fromJson(
                    data.getString("aliases"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbAliases.entrySet()) {
                    aliases.put(item.getKey().toLowerCase(), item.getValue());
                }
            }

            if (data.getString("prefixes", null) != null) {
                HashMap<String, String> dbPrefixes = new Gson().fromJson(
                    data.getString("prefixes"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbPrefixes.entrySet()) {
                    prefixes.put(item.getKey().toLowerCase(), item.getValue());
                }
            }

            if (data.getString("claimable_roles", null) != null) {
                HashMap<String, String> dbSelfAssignableRoles = new Gson().fromJson(
                    data.getString("claimable_roles"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbSelfAssignableRoles.entrySet()) {
                    selfAssignableRoles.put(item.getKey(), item.getValue().toLowerCase());
                }
            }

            if (data.getString("channels", null) != null) {
                HashMap<String, Object> dbChannels = new Gson().fromJson(
                    data.getString("channels"),
                    new TypeToken<HashMap<String, Object>>() {
                    }.getType());

                for (Map.Entry<String, Object> item : dbChannels.entrySet()) {
                    LinkedTreeMap<String, Object> value = (LinkedTreeMap<String, Object>) item.getValue();
                    value.put("id", item.getKey());

                    channels.add(new ChannelTransformer(new DataRow(value)));
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

    public Map<String, String> getSelfAssignableRoles() {
        return selfAssignableRoles;
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public List<ChannelTransformer> getChannels() {
        return channels;
    }

    public ChannelTransformer getChannel(String id) {
        for (ChannelTransformer channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }
        return null;
    }

    public boolean createChannelTransformer(TextChannel channel) {
        if (!Objects.equals(channel.getGuild().getId(), getId())) {
            throw new RuntimeException(String.format("The given channel belongs to a different guild. Channel ID: %s Channel Guild ID: %s | Guild ID: %s",
                channel.getId(), channel.getGuild().getId(), getId()
            ));
        }

        if (getChannel(channel.getId()) != null) {
            return false;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", channel.getId());
        channels.add(new ChannelTransformer(new DataRow(data)));

        return true;
    }

    public String channelsToJson() {
        Map<String, Object> objects = new HashMap<>();
        if (channels.isEmpty()) {
            return null;
        }

        for (ChannelTransformer transformer : channels) {
            objects.put(transformer.getId(), transformer.toMap());
        }

        return new Gson().toJson(objects);
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
