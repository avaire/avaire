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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddSelfAssignableCommand extends Command {

    public AddSelfAssignableCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Add Self Assignable Role Command";
    }

    @Override
    public String getDescription() {
        return "Adds a role to the self-assignable roles list, any role on the list can be claimed by users when they use `.iam <role>`.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <role>` - Adds the mentioned role to the self-assignable roles list.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command DJ`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("asar");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
                "require:user,general.administrator",
                "throttle:guild,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument, the `role` argument is required.");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(message, args[0]);
        if (role == null) {
            MessageFactory.makeWarning(message, "<@%s> Invalid role, I couldn't find any role called **%s**",
                    message.getAuthor().getId(), args[0]
            ).queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(message, role)) {
            return false;
        }

        try {
            GuildTransformer transformer = GuildController.fetchGuild(orion, message);

            transformer.getSelfAssignableRoles().put(role.getId(), role.getName());
            orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", message.getGuild().getId())
                    .update(statement -> {
                        statement.set("claimable_roles", new Gson().toJson(transformer.getSelfAssignableRoles()));
                    });

            MessageFactory.makeSuccess(message, "Role **%s** role has been added to the self-assignable list.",
                    role.getName()
            ).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
