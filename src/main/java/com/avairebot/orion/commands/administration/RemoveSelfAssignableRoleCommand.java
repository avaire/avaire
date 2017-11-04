package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import com.google.gson.Gson;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RemoveSelfAssignableRoleCommand extends Command {

    public RemoveSelfAssignableRoleCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Remove Self Assignable Role Command";
    }

    @Override
    public String getDescription() {
        return "Removes a role from the self-assignable roles list, any role on the list can be claimed by users when they use `.iam <role>`.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <role>` - Removes the mentioned role from the self-assignable roles list.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command DJ`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rsar");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument, the `role` argument is required.");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(message, args[0]);
        if (role == null) {
            MessageFactory.makeWarning(message, ":user Invalid role, I couldn't find any role called **:role**")
                .set("role", args[0])
                .queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(message, role)) {
            return false;
        }

        try {
            GuildTransformer transformer = GuildController.fetchGuild(orion, message);

            transformer.getSelfAssignableRoles().remove(role.getId());
            orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", new Gson().toJson(transformer.getSelfAssignableRoles()));
                });

            MessageFactory.makeSuccess(message, "Role **:role** role has been removed from the self-assignable list.")
                .set("role", role.getName()).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
