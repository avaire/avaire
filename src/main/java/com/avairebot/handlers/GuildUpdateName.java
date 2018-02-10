package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.handlers.EventHandler;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;

import java.sql.SQLException;

public class GuildUpdateName extends EventHandler {

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GuildUpdateName(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", event.getGuild().getId())
                .update(statement -> statement.set("name", event.getGuild().getName(), true));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
