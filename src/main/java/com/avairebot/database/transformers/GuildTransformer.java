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

package com.avairebot.database.transformers;

import com.avairebot.AvaIre;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.DataRow;
import com.avairebot.utilities.NumberUtil;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;

public class GuildTransformer extends Transformer {

    private static final GuildTypeTransformer partnerTypeTransformer = new PartnerGuildTypeTransformer();

    private final Map<String, String> aliases = new HashMap<>();
    private final Map<String, String> prefixes = new HashMap<>();
    private final Map<String, String> selfAssignableRoles = new HashMap<>();
    private final Map<Integer, String> levelRoles = new HashMap<>();
    private final Map<String, Map<String, String>> modules = new HashMap<>();
    private final List<ChannelTransformer> channels = new ArrayList<>();
    private final Set<Long> levelExemptChannels = new HashSet<>();

    private final GuildTypeTransformer guildType;
    private boolean partner;

    private String id;
    private String name;
    private String nameRaw;
    private String locale;
    private boolean levels = false;
    private boolean levelAlerts = false;
    private boolean levelHierarchy = false;
    private boolean musicMessages = true;
    private String levelChannel = null;
    private String autorole = null;
    private String modlog = null;
    private String musicChannelText = null;
    private String musicChannelVoice = null;
    private int modlogCase = 0;
    private int defaultVolume = 100;
    private double levelModifier = -1;
    private DJGuildLevel djGuildLevel = null;

    public GuildTransformer(Guild guild) {
        super(null);

        locale = null;
        id = guild.getId();
        name = guild.getName();
        nameRaw = guild.getName();

        partner = guild.getRegion().isVip();
        guildType = partner ? partnerTypeTransformer : new GuildTypeTransformer(data);
    }

    public GuildTransformer(Guild guild, DataRow data) {
        super(data);

        partner = guild.getRegion().isVip();

        if (hasData()) {
            id = data.getString("id");
            name = data.getString("name");
            nameRaw = data.get("name").toString();
            locale = data.getString("local");

            levels = data.getBoolean("levels");
            levelAlerts = data.getBoolean("level_alerts");
            levelHierarchy = data.getBoolean("hierarchy");
            levelChannel = data.getString("level_channel");
            levelModifier = data.getDouble("level_modifier", -1);
            autorole = data.getString("autorole");
            modlog = data.getString("modlog");
            musicChannelText = data.getString("music_channel_text");
            musicChannelVoice = data.getString("music_channel_voice");
            musicMessages = data.getBoolean("music_messages", true);
            modlogCase = data.getInt("modlog_case");
            djGuildLevel = DJGuildLevel.fromId(data.getInt("dj_level", DJGuildLevel.getNormal().getId()));
            defaultVolume = data.getInt("default_volume", 100);

            // Sets the default volume to a value between 10 and 100.
            defaultVolume = NumberUtil.getBetween(defaultVolume, 10, 100);

            // Sets the discord partner value if the guild isn't already a Discord partner.
            if (!partner) {
                partner = data.getBoolean("partner", false);
            }

            if (data.getString("aliases", null) != null) {
                HashMap<String, String> dbAliases = AvaIre.gson.fromJson(
                    data.getString("aliases"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbAliases.entrySet()) {
                    aliases.put(item.getKey().toLowerCase(), item.getValue());
                }
            }

            if (data.getString("prefixes", null) != null) {
                HashMap<String, String> dbPrefixes = AvaIre.gson.fromJson(
                    data.getString("prefixes"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbPrefixes.entrySet()) {
                    prefixes.put(item.getKey().toLowerCase(), item.getValue());
                }
            }

            if (data.getString("claimable_roles", null) != null) {
                HashMap<String, String> dbSelfAssignableRoles = AvaIre.gson.fromJson(
                    data.getString("claimable_roles"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbSelfAssignableRoles.entrySet()) {
                    selfAssignableRoles.put(item.getKey(), item.getValue().toLowerCase());
                }
            }

            if (data.getString("level_roles", null) != null) {
                HashMap<String, String> dbLevelRoles = AvaIre.gson.fromJson(
                    data.getString("level_roles"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());

                for (Map.Entry<String, String> item : dbLevelRoles.entrySet()) {
                    levelRoles.put(NumberUtil.parseInt(item.getKey(), -1), item.getValue().toLowerCase());
                }
            }

            if (data.getString("level_exempt_channels", null) != null) {
                List<String> dbExemptExperienceChannels = AvaIre.gson.fromJson(
                    data.getString("level_exempt_channels"),
                    new TypeToken<List<String>>() {
                    }.getType());

                for (String channelId : dbExemptExperienceChannels) {
                    try {
                        levelExemptChannels.add(
                            Long.parseLong(channelId)
                        );
                    } catch (NumberFormatException ignored) {
                        //
                    }
                }
            }

            if (data.getString("modules", null) != null) {
                HashMap<String, Map<String, String>> dbModules = AvaIre.gson.fromJson(
                    data.getString("modules"),
                    new TypeToken<HashMap<String, Map<String, String>>>() {
                    }.getType());

                for (Map.Entry<String, Map<String, String>> item : dbModules.entrySet()) {
                    modules.put(item.getKey(), item.getValue());
                }
            }

            if (data.getString("channels", null) != null) {
                HashMap<String, Object> dbChannels = AvaIre.gson.fromJson(
                    data.getString("channels"),
                    new TypeToken<HashMap<String, Object>>() {
                    }.getType());

                for (Map.Entry<String, Object> item : dbChannels.entrySet()) {
                    // noinspection unchecked
                    LinkedTreeMap<String, Object> value = (LinkedTreeMap<String, Object>) item.getValue();
                    value.put("id", item.getKey());

                    channels.add(new ChannelTransformer(new DataRow(value), this));
                }
            }
        }

        guildType = partner ? partnerTypeTransformer : new GuildTypeTransformer(data);

        reset();
    }

    public String getId() {
        return id;
    }

    public GuildTypeTransformer getType() {
        return guildType;
    }

    public String getName() {
        return name;
    }

    public String getNameRaw() {
        return nameRaw;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String code) {
        this.locale = code;
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

    public boolean isLevelHierarchy() {
        return levelHierarchy;
    }

    public void setLevelHierarchy(boolean levelHierarchy) {
        this.levelHierarchy = levelHierarchy;
    }

    public String getLevelChannel() {
        return levelChannel;
    }

    public void setLevelChannel(String levelChannel) {
        this.levelChannel = levelChannel;
    }

    public Map<Integer, String> getLevelRoles() {
        return levelRoles;
    }

    public double getLevelModifier() {
        return levelModifier;
    }

    public void setLevelModifier(double levelModifier) {
        this.levelModifier = levelModifier;
    }

    public Set<Long> getLevelExemptChannels() {
        return levelExemptChannels;
    }

    public String getAutorole() {
        return autorole;
    }

    public void setAutorole(String autorole) {
        this.autorole = autorole;
    }

    public String getMusicChannelText() {
        return musicChannelText;
    }

    public void setMusicChannelText(String musicChannelText) {
        this.musicChannelText = musicChannelText;
    }

    public String getMusicChannelVoice() {
        return musicChannelVoice;
    }

    public void setMusicChannelVoice(String musicChannelVoice) {
        this.musicChannelVoice = musicChannelVoice;
    }

    public boolean isMusicMessages() {
        return musicMessages;
    }

    public void setMusicMessages(boolean musicMessages) {
        this.musicMessages = musicMessages;
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

    public int getDefaultVolume() {
        return defaultVolume;
    }

    public void setDefaultVolume(int defaultVolume) {
        this.defaultVolume = defaultVolume;
    }

    public boolean isPartner() {
        return partner;
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

        return AvaIre.gson.toJson(objects);
    }
}
