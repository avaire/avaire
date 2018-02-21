package com.avairebot.database.transformers;

import com.avairebot.AvaIre;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.avairebot.time.Carbon;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;

public class GuildTransformer extends Transformer {

    private final Map<String, String> aliases = new HashMap<>();
    private final Map<String, String> prefixes = new HashMap<>();
    private final Map<String, String> selfAssignableRoles = new HashMap<>();
    private final Map<String, Map<String, String>> modules = new HashMap<>();
    private final List<ChannelTransformer> channels = new ArrayList<>();

    private final GuildTypeTransformer guildType;

    private boolean levels = false;
    private boolean levelAlerts = false;
    private String levelChannel = null;
    private String autorole = null;
    private String modlog = null;
    private int modlogCase = 0;
    private DJGuildLevel djGuildLevel = null;

    public GuildTransformer(DataRow data) {
        super(data);

        guildType = new GuildTypeTransformer(data);

        if (hasData()) {
            levels = data.getBoolean("levels");
            levelAlerts = data.getBoolean("level_alerts");
            levelChannel = data.getString("level_channel");
            autorole = data.getString("autorole");
            modlog = data.getString("modlog");
            modlogCase = data.getInt("modlog_case");
            djGuildLevel = DJGuildLevel.fromId(data.getInt("dj_level", DJGuildLevel.getNormal().getId()));

            if (data.getString("aliases", null) != null) {
                HashMap<String, String> dbAliases = AvaIre.GSON.fromJson(
                    data.getString("aliases"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbAliases.entrySet()) {
                    aliases.put(item.getKey().toLowerCase(), item.getValue());
                }
            }

            if (data.getString("prefixes", null) != null) {
                HashMap<String, String> dbPrefixes = AvaIre.GSON.fromJson(
                    data.getString("prefixes"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbPrefixes.entrySet()) {
                    prefixes.put(item.getKey().toLowerCase(), item.getValue());
                }
            }

            if (data.getString("claimable_roles", null) != null) {
                HashMap<String, String> dbSelfAssignableRoles = AvaIre.GSON.fromJson(
                    data.getString("claimable_roles"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbSelfAssignableRoles.entrySet()) {
                    selfAssignableRoles.put(item.getKey(), item.getValue().toLowerCase());
                }
            }

            if (data.getString("modules", null) != null) {
                HashMap<String, Map<String, String>> dbModules = AvaIre.GSON.fromJson(
                    data.getString("modules"),
                    new TypeToken<HashMap<String, Map<String, String>>>() {
                    }.getType());

                for (Map.Entry<String, Map<String, String>> item : dbModules.entrySet()) {
                    modules.put(item.getKey(), item.getValue());
                }
            }

            if (data.getString("channels", null) != null) {
                HashMap<String, Object> dbChannels = AvaIre.GSON.fromJson(
                    data.getString("channels"),
                    new TypeToken<HashMap<String, Object>>() {
                    }.getType());

                for (Map.Entry<String, Object> item : dbChannels.entrySet()) {
                    LinkedTreeMap<String, Object> value = (LinkedTreeMap<String, Object>) item.getValue();
                    value.put("id", item.getKey());

                    channels.add(new ChannelTransformer(new DataRow(value), this));
                }
            }
        }
    }

    public String getId() {
        return data.getString("id");
    }

    public GuildTypeTransformer getType() {
        return guildType;
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

    public String getLocale() {
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

    public String getModlog() {
        return modlog;
    }

    public void setModlog(String modlogChannelId) {
        this.modlog = modlogChannelId;
    }

    public int getModlogCase() {
        return modlogCase;
    }

    public void setModlogCase(int modlogCase) {
        this.modlogCase = modlogCase;
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

    public Map<String, Map<String, String>> getCategories() {
        return modules;
    }

    public DJGuildLevel getDJLevel() {
        if (djGuildLevel == null) {
            djGuildLevel = DJGuildLevel.getNormal();
        }
        return djGuildLevel;
    }

    public void setDJLevel(DJGuildLevel djGuildLevel) {
        this.djGuildLevel = djGuildLevel;
    }

    @CheckReturnValue
    public ChannelTransformer getChannel(String id) {
        return getChannel(id, true);
    }

    @CheckReturnValue
    public ChannelTransformer getChannel(String id, boolean createIfDontExists) {
        for (ChannelTransformer channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }

        if (createIfDontExists && createChannelTransformer(getId(), id)) {
            return getChannel(id, false);
        }

        return null;
    }

    public boolean createChannelTransformer(@Nonnull String guildId, @Nonnull String channelId) {
        if (!Objects.equals(guildId, getId())) {
            throw new RuntimeException(String.format("The given channel belongs to a different guild. Channel ID: %s Channel Guild ID: %s | Guild ID: %s",
                channelId, guildId, getId()
            ));
        }

        if (getChannel(channelId, false) != null) {
            return false;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", channelId);
        channels.add(new ChannelTransformer(new DataRow(data), this));

        return true;
    }

    public boolean createChannelTransformer(TextChannel channel) {
        return createChannelTransformer(channel.getGuild().getId(), channel.getId());
    }

    public String channelsToJson() {
        Map<String, Object> objects = new HashMap<>();
        if (channels.isEmpty()) {
            return null;
        }

        for (ChannelTransformer transformer : channels) {
            objects.put(transformer.getId(), transformer.toMap());
        }

        return AvaIre.GSON.toJson(objects);
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
