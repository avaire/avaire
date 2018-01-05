package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

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

        AvaIre.getLogger().info("Joined guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        AvaIre.getLogger().info("Left guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName());
    }
}
