package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserIdCommand extends Command {

    public UserIdCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "User ID Command";
    }

    @Override
    public String getDescription() {
        return "Shows your Discord account user ID, or the ID of the user tagged in the command.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [user]`");
    }

    @Override
    public String getExampleUsage() {
        return "`:command @Senither`\n`:command alexis`\n`:command 88739639380172800`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("userid", "uid");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        User user = message.getAuthor();
        if (args.length > 0) {
            user = MentionableUtil.getUser(message, args);
        }

        if (user == null) {
            return sendErrorMessage(message, "I found no users with the name or ID of `%s`", args[0]);
        }

        MessageFactory.makeSuccess(message, ":id: of the user **:target** is `:targetid`")
            .set("target", user.getAsMention())
            .set("targetid", user.getId())
            .queue();
        return true;
    }
}
