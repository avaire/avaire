package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.User;

import java.util.Collections;
import java.util.List;

public class UserAvatarCommand extends Command {

    public UserAvatarCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "User Avatar Command";
    }

    @Override
    public String getDescription() {
        return "Get the profile picture of someone on the server by name, id, or mentions.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user | user id>` - Gets the avatar of the given user.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("avatar");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = context.getAuthor();
        if (args.length > 0) {
            user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
        }

        if (user == null) {
            return sendErrorMessage(context, "I found no users with the name or ID of `{0}`", args[0]);
        }

        String avatarUrl = generateAvatarUrl(user);
        MessageFactory.makeEmbeddedMessage(context.getChannel())
            .setTitle(user.getName() + "#" + user.getDiscriminator() + "'s Avatar", avatarUrl)
            .requestedBy(context.getMember())
            .setImage(avatarUrl)
            .queue();

        return true;
    }

    private String generateAvatarUrl(User user) {
        String avatarUrl = user.getEffectiveAvatarUrl();
        String[] parts = avatarUrl.split("\\.");

        String extension = parts[parts.length - 1];

        return avatarUrl + "?size=256&." + extension;
    }
}
