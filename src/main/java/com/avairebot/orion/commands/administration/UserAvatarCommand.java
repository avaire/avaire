package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Collections;
import java.util.List;

public class UserAvatarCommand extends Command {

    public UserAvatarCommand(Orion orion) {
        super(orion, false);
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
    public boolean onCommand(Message message, String[] args) {
        User user = message.getAuthor();
        if (args.length > 0) {
            user = MentionableUtil.getUser(message, args);
        }

        if (user == null) {
            return sendErrorMessage(message, "I found no users with the name or ID of `%s`", args[0]);
        }

        String avatarUrl = generateAvatarUrl(user);
        MessageFactory.makeEmbeddedMessage(message.getChannel())
            .setTitle(user.getName() + "#" + user.getDiscriminator() + "'s Avatar", avatarUrl)
            .setImage(avatarUrl)
            .queue();

        return true;
    }

    private String generateAvatarUrl(User user) {
        String avatarUrl = user.getEffectiveAvatarUrl();
        String[] parts = avatarUrl.split("\\.");

        String extension = parts[parts.length - 1];

        return avatarUrl + "?size=1024&." + extension;
    }
}
