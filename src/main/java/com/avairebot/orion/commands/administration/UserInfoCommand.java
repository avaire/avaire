package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.PlaceholderMessage;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.time.Carbon;
import com.avairebot.orion.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoCommand extends Command {

    public UserInfoCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "User Info Command";
    }

    @Override
    public String getDescription() {
        return "Shows information about the user tat ran the command, or the mentioned user.";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("userinfo", "uinfo");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Member member = message.getMember();
        if (args.length > 0) {
            User user = MentionableUtil.getUser(message, args);
            if (user == null) {
                return sendErrorMessage(message, "I found no users with the name or ID of `%s`", args[0]);
            }
            member = message.getGuild().getMember(user);
        }

        Carbon joinedDate = Carbon.createFromOffsetDateTime(member.getJoinDate());
        Carbon createdDate = Carbon.createFromOffsetDateTime(member.getUser().getCreationTime());

        PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(message.getChannel(), getRoleColor(member.getRoles()),
            new MessageEmbed.Field("Username", member.getUser().getName(), true),
            new MessageEmbed.Field("User ID", member.getUser().getId(), true),
            new MessageEmbed.Field("Joined Server", createdDate.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(createdDate) + "*", true),
            new MessageEmbed.Field("Joined Discord", joinedDate.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(joinedDate) + "*", true)
        ).setThumbnail(member.getUser().getEffectiveAvatarUrl());

        String memberRoles = "*This user is not in any roles*";
        if (!member.getRoles().isEmpty()) {
            memberRoles = member.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining("\n"));
        }

        placeholderMessage.addField(new MessageEmbed.Field(String.format("Roles (%s)", member.getRoles().size()), memberRoles, true));
        placeholderMessage.addField(new MessageEmbed.Field("Servers", "" + member.getUser().getMutualGuilds().size() + " the bot knows about", true));

        placeholderMessage.queue();
        return true;
    }

    private String shortenDiffForHumans(Carbon carbon) {
        String diff = carbon.diffForHumans();
        if (!diff.contains("and")) {
            return diff;
        }
        return diff.split("and")[0] + "ago";
    }

    private Color getRoleColor(List<Role> roles) {
        for (Role role : roles) {
            if (role.getColor() != null) return role.getColor();
        }
        return Color.decode("#E91E63");
    }
}
