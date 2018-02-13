package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.logger.EventLogger;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;

import java.sql.SQLException;

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
        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.inc();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).inc();
        EventLogger.logGuildJoin(avaire, event);

        AvaIre.getLogger().info("Joined guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());
    }

    public void onGuildLeave(GuildLeaveEvent event) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.dec();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).dec();
        EventLogger.logGuildLeave(avaire, event);

        AvaIre.getLogger().info("Left guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());
    }
}
