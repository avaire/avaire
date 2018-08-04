package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.ConsoleColor;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.metrics.Metrics;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
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

        channel.sendMessage(
            new EmbedBuilder()
                .setColor(Color.decode("#66BB6A"))
                .setTimestamp(Instant.now())
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
        AvaIre.getLogger().info(ConsoleColor.format(
            "%redLeft guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName() + "%reset"
        ));

        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.dec();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).dec();

        TextChannel channel = avaire.getShardManager().getTextChannelById(DiscordConstants.ACTIVITY_LOG_CHANNEL_ID);
        if (channel == null) {
            return;
        }

        channel.sendMessage(
            new EmbedBuilder()
                .setColor(Color.decode("#EF5350"))
                .setTimestamp(Instant.now())
                .addField("Removed", String.format("%s (ID: %s)",
                    event.getGuild().getName(), event.getGuild().getId()
                ), false)
                .build()
        ).queue(null, RestActionUtil.ignore);
    }
}
