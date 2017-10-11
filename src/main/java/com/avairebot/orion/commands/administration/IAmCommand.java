package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.util.Collections;
import java.util.List;

public class IAmCommand extends Command {

    public IAmCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "I Am Command";
    }

    @Override
    public String getDescription() {
        return "Gives you the role with the given name if it is in the self-assignable list of roles.";
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
        return Collections.singletonList("iam");
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

        GuildTransformer transformer = GuildController.fetchGuild(orion, message);
        if (!transformer.getSelfAssignableRoles().containsValue(role.getName().toLowerCase())) {
            MessageFactory.makeWarning(message, "<@%s> Invalid role, **%s** is not a self-assignable role.",
                message.getAuthor().getId(), args[0]
            ).queue();
            return false;
        }

        if (!RoleUtil.hasRole(message.getMember(), role)) {
            message.getGuild().getController().addSingleRoleToMember(message.getMember(), role).queue();
        }

        MessageFactory.makeSuccess(message, "<@%s> You now have the **%s** role!",
            message.getAuthor().getId(), role.getName()
        ).queue();
        return true;
    }
}
