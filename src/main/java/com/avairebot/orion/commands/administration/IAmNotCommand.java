package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IAmNotCommand extends Command {

    public IAmNotCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "I Am Command";
    }

    @Override
    public String getDescription() {
        return "Removes the role with the given name from you if it is in the self-assignable list of roles.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <role>`");
    }

    @Override
    public String getExampleUsage() {
        return "`:command DJ`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("iamnot", "iamn");
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

        GuildTransformer transformer = GuildController.fetchGuild(orion, message);
        if (!transformer.getSelfAssignableRoles().containsValue(role.getName().toLowerCase())) {
            MessageFactory.makeWarning(message, ":user Invalid role, **:role** is not a self-assignable role.")
                .set("role", args[0])
                .queue();
            return false;
        }

        if (RoleUtil.hasRole(message.getMember(), role)) {
            message.getGuild().getController().removeSingleRoleFromMember(message.getMember(), role).queue();
        }

        MessageFactory.makeSuccess(message, ":user You no longer have the **:role** role!")
            .set("role", role.getName())
            .queue();
        return true;
    }
}
