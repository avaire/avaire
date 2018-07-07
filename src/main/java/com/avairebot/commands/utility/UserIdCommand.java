package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserIdCommand extends Command {

    public UserIdCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command @Senither`",
            "`:command alexis`",
            "`:command 88739639380172800`"
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(UserInfoCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("userid", "uid");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = context.getAuthor();
        if (args.length > 0) {
            user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
        }

        if (user == null) {
            return sendErrorMessage(context, "errors.noUsersWithNameOrId", args[0]);
        }

        context.makeSuccess(context.i18n("message"))
            .set("target", user.getAsMention())
            .set("targetid", user.getId())
            .queue();

        return true;
    }
}
