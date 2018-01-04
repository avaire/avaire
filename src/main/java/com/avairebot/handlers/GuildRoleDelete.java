package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;

import java.sql.SQLException;

public class GuildRoleDelete extends EventHandler {

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GuildRoleDelete(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null || transformer.getSelfAssignableRoles().isEmpty()) {
            return;
        }

        if (!transformer.getSelfAssignableRoles().containsKey(event.getRole().getId())) {
            return;
        }

        try {
            transformer.getSelfAssignableRoles().remove(event.getRole().getId());
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", event.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", AvaIre.GSON.toJson(transformer.getSelfAssignableRoles()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
