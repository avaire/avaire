package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.metrics.Metrics;
import com.avairebot.scheduler.SendWebhookMessagesJob;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;

import java.awt.*;
import java.time.Instant;

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

        User owner = event.getGuild().getOwner().getUser();
        SendWebhookMessagesJob.addMessageEmbed(avaire, new EmbedBuilder()
            .setColor(Color.decode("#66BB6A"))
            .setTimestamp(Instant.now())
            .addField("Added", String.format("%s (ID: %s)",
                event.getGuild().getName(), event.getGuild().getId()
            ), false)
            .addField("Owner", String.format("%s#%s (ID: %s)",
                owner.getName(), owner.getDiscriminator(), owner.getId()
            ), false)
            .build()
        );
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.dec();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).dec();

        AvaIre.getLogger().info("Left guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());

        SendWebhookMessagesJob.addMessageEmbed(avaire, new EmbedBuilder()
            .setColor(Color.decode("#EF5350"))
            .setTimestamp(Instant.now())
            .addField("Removed", String.format("%s (ID: %s)",
                event.getGuild().getName(), event.getGuild().getId()
            ), false).build()
        );
    }

    @Override
    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {
        Metrics.geoTracker.labels(event.getOldRegion().getName()).dec();
        Metrics.geoTracker.labels(event.getNewRegion().getName()).inc();
    }
}
