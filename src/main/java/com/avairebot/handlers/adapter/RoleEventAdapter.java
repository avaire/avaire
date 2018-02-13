package com.avairebot.handlers.adapter;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.handlers.EventAdapter;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;

import java.sql.SQLException;

public class RoleEventAdapter extends EventAdapter {

    /**
     * Instantiates the event adapter and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public RoleEventAdapter(AvaIre avaire) {
        super(avaire);
    }

    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null || transformer.getSelfAssignableRoles().isEmpty()) {
            return;
        }

        if (!transformer.getSelfAssignableRoles().containsKey(event.getRole().getId())) {
            return;
        }

        try {
            transformer.getSelfAssignableRoles().put(event.getRole().getId(), event.getRole().getName().toLowerCase());
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", event.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", AvaIre.GSON.toJson(transformer.getSelfAssignableRoles()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onRoleDelete(RoleDeleteEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());
        if (transformer == null) {
            return;
        }

        handleAutoroles(event, transformer);
        handleSelfAssignableRoles(event, transformer);
    }

    private void handleAutoroles(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getAutorole() == null || !event.getRole().getId().equals(transformer.getAutorole())) {
            return;
        }

        try {
            transformer.setAutorole(null);
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", event.getGuild().getId())
                .update(statement -> statement.set("autorole", null));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSelfAssignableRoles(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getSelfAssignableRoles().isEmpty() || !transformer.getSelfAssignableRoles().containsKey(event.getRole().getId())) {
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
