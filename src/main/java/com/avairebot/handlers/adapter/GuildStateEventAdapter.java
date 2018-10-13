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

package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.GuildMusicManager;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.chat.ConsoleColor;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.metrics.Metrics;
import com.avairebot.scheduler.ScheduleHandler;
import com.avairebot.scheduler.tasks.MusicActivityTask;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;

public class GuildStateEventAdapter extends EventAdapter {

    /**
     * Instantiates the event adapter and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GuildStateEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> statement.set("name", event.getGuild().getName(), true));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {
        Metrics.geoTracker.labels(event.getOldRegion().getName()).dec();
        Metrics.geoTracker.labels(event.getNewRegion().getName()).inc();
    }

    public void onGuildJoin(GuildJoinEvent event) {
        AvaIre.getLogger().info(ConsoleColor.format(
            "%greenJoined guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName() + "%reset"
        ));

        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.inc();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).inc();

        TextChannel channel = avaire.getShardManager().getTextChannelById(DiscordConstants.ACTIVITY_LOG_CHANNEL_ID);
        if (channel == null) {
            return;
        }

        User owner = event.getGuild().getOwner().getUser();

        double guildMembers = event.getGuild().getMembers().stream().filter(member -> !member.getUser().isBot()).count();
        double guildBots = event.getGuild().getMembers().stream().filter(member -> member.getUser().isBot()).count();
        double percentage = (guildBots / (guildBots + guildMembers)) * 100;

        channel.sendMessage(
            new EmbedBuilder()
                .setColor(Color.decode("#66BB6A"))
                .setTimestamp(Instant.now())
                .setFooter(String.format("%s Users, and %s Bots, %s Bots",
                    NumberUtil.formatNicely(guildMembers),
                    NumberUtil.formatNicely(guildBots),
                    NumberUtil.formatNicelyWithDecimals(percentage) + "%"
                ), null)
                .addField("Added", String.format("%s (ID: %s)",
                    event.getGuild().getName(), event.getGuild().getId()
                ), false)
                .addField("Owner", String.format("%s#%s (ID: %s)",
                    owner.getName(), owner.getDiscriminator(), owner.getId()
                ), false)
                .build()
        ).queue(null, RestActionUtil.ignore);
    }

    public void onGuildLeave(GuildLeaveEvent event) {
        handleSendGuildLeaveWebhook(event.getGuild());
        handleAudioConnectionOnGuildLeave(event.getGuild());
    }

    private void handleSendGuildLeaveWebhook(Guild guild) {
        AvaIre.getLogger().info(ConsoleColor.format(
            "%redLeft guild with an ID of " + guild.getId() + " called: " + guild.getName() + "%reset"
        ));

        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.dec();
        Metrics.geoTracker.labels(guild.getRegion().getName()).dec();

        TextChannel channel = avaire.getShardManager().getTextChannelById(DiscordConstants.ACTIVITY_LOG_CHANNEL_ID);
        if (channel == null) {
            return;
        }

        channel.sendMessage(
            new EmbedBuilder()
                .setColor(Color.decode("#EF5350"))
                .setTimestamp(Instant.now())
                .addField("Removed", String.format("%s (ID: %s)",
                    guild.getName(), guild.getId()
                ), false)
                .build()
        ).queue(null, RestActionUtil.ignore);
    }

    private void handleAudioConnectionOnGuildLeave(Guild guild) {
        long guildId = guild.getIdLong();

        ScheduleHandler.getScheduler().submit(() -> {
            GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler()
                .musicManagers.remove(guildId);

            if (musicManager == null) {
                return;
            }

            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().getQueue().clear();

            MusicActivityTask.missingListener.remove(guildId);
            MusicActivityTask.playerPaused.remove(guildId);
            MusicActivityTask.emptyQueue.remove(guildId);

            musicManager.getScheduler().nextTrack(false);

            if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
                JdaLink link = LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink()
                    .getLink(String.valueOf(guildId));


                if (!LavalinkManager.LavalinkManagerHolder.lavalink.isLinkBeingDestroyed(link)) {
                    link.destroy();
                }
            }
        });
    }
}
