package com.avairebot.orion.handlers;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.google.gson.Gson;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;

import java.sql.SQLException;

public class GuildRoleDelete extends EventHandler {

    /**
     * Instantiates the event handler and sets the orion class instance.
     *
     * @param orion The Orion application class instance.
     */
    public GuildRoleDelete(Orion orion) {
        super(orion);
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, event.getGuild());
        if (transformer == null || transformer.getSelfAssignableRoles().isEmpty()) {
            return;
        }

        if (!transformer.getSelfAssignableRoles().containsKey(event.getRole().getId())) {
            return;
        }

        try {
            transformer.getSelfAssignableRoles().remove(event.getRole().getId());
            orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", event.getGuild().getId())
                    .update(statement -> {
                        statement.set("claimable_roles", new Gson().toJson(transformer.getSelfAssignableRoles()));
                    });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
