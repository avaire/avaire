package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "self-assignable-role-command")
public class AddSelfAssignableRoleCommand extends Command {

    public AddSelfAssignableRoleCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command DJ`");
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
            MessageFactory.makeWarning(message, ":user Invalid role, I couldn't find any role called **:role**")
                .set("role", args[0])
                .queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(message, role)) {
            return false;
        }

        try {
            GuildTransformer transformer = GuildController.fetchGuild(avaire, message);

            transformer.getSelfAssignableRoles().put(role.getId(), role.getName().toLowerCase());
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", AvaIre.GSON.toJson(transformer.getSelfAssignableRoles()), true);
                });

            MessageFactory.makeSuccess(message, "Role **:role** role has been added to the self-assignable list.")
                .set("role", role.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
