package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;

public class GuildJoinLeave extends EventHandler {

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GuildJoinLeave(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.inc();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).inc();

        AvaIre.getLogger().info("Joined guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.dec();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).dec();

        AvaIre.getLogger().info("Left guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());
    }

    @Override
    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {
        Metrics.geoTracker.labels(event.getOldRegion().getName()).dec();
        Metrics.geoTracker.labels(event.getNewRegion().getName()).inc();
    }
}
