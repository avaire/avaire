package com.avairebot.database.controllers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.CheckReturnValue;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GuildController {

    private static final Cache<Object, Object> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .expireAfterWrite(4, TimeUnit.MINUTES)
        .build();

    private static final String[] REQUIRED_GUILD_ITEMS = new String[]{
        "guild_types.name as type_name", "guild_types.limits as type_limits",
        "guilds.id", "guilds.name", "guilds.icon", "guilds.local", "guilds.channels", "guilds.modules", "guilds.level_roles", "guilds.claimable_roles",
        "guilds.prefixes", "guilds.aliases", "guilds.default_volume", "guilds.dj_level", "guilds.modlog_case", "guilds.modlog", "guilds.autorole",
        "guilds.level_channel", "guilds.level_alerts", "guilds.levels", "guilds.music_channel_text", "guilds.music_channel_voice"
    };

    @CheckReturnValue
    public static GuildTransformer fetchGuild(AvaIre avaire, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }
        return fetchGuild(avaire, message.getGuild());
    }

    @CheckReturnValue
    public static GuildTransformer fetchGuild(AvaIre avaire, Guild guild) {
        return (GuildTransformer) CacheUtil.getUncheckedUnwrapped(cache, guild.getIdLong(), () -> {
            try {
                GuildTransformer transformer = new GuildTransformer(avaire.getDatabase()
                    .newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .select(REQUIRED_GUILD_ITEMS)
                    .leftJoin("guild_types", "guilds.type", "guild_types.id")
                    .where("guilds.id", guild.getId())
                    .get().first());

                if (!transformer.hasData()) {
                    try {
                        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                            .insert(statement -> {
                                statement.set("id", guild.getId())
                                    .set("owner", guild.getOwner().getUser().getId())
                                    .set("name", guild.getName(), true)
                                    .set("roles_data", buildRoleData(guild.getRoles()), true)
                                    .set("channels_data", buildChannelData(guild.getTextChannels()), true);

                                if (guild.getIconId() != null) {
                                    statement.set("icon", guild.getIconId());
                                }
                            });
                    } catch (Exception ex) {
                        AvaIre.getLogger().error(ex.getMessage(), ex);
                    }
                }

                return transformer;
            } catch (SQLException ex) {
                AvaIre.getLogger().error(ex.getMessage(), ex);
                return null;
            }
        });
    }

    public static String buildChannelData(List<TextChannel> textChannels) {
        List<Map<String, Object>> channels = new ArrayList<>();
        for (TextChannel channel : textChannels) {
            Map<String, Object> item = new HashMap<>();

            item.put("id", channel.getId());
            item.put("name", channel.getName());
            item.put("position", channel.getPosition());

            channels.add(item);
        }
        return AvaIre.GSON.toJson(channels);
    }

    public static String buildRoleData(List<Role> roles) {
        List<Map<String, Object>> rolesMap = new ArrayList<>();
        for (Role role : roles) {
            if (role.isPublicRole()) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();

            item.put("id", role.getId());
            item.put("name", role.getName());
            item.put("position", role.getPosition());
            item.put("permissions", role.getPermissionsRaw());

            item.put("color", role.getColor() == null ? null
                : Integer.toHexString(role.getColor().getRGB()).substring(2)
            );

            rolesMap.add(item);
        }
        return AvaIre.GSON.toJson(rolesMap);
    }

    public static void forgetCache(long guildId) {
        cache.invalidate(guildId);
    }
}
