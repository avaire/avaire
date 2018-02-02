package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoCommand extends Command {

    public UserInfoCommand(AvaIre avaire) {
        super(avaire, false);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        Member member = context.getMember();
        if (args.length > 0) {
            User user = MentionableUtil.getUser(context.getMessage(), args);
            if (user == null) {
                return sendErrorMessage(context, "I found no users with the name or ID of `%s`", args[0]);
            }
            member = context.getGuild().getMember(user);
        }

        Carbon joinedDate = Carbon.createFromOffsetDateTime(member.getJoinDate());
        Carbon createdDate = Carbon.createFromOffsetDateTime(member.getUser().getCreationTime());

        PlaceholderMessage placeholderMessage = context.makeEmbeddedMessage(getRoleColor(member.getRoles()),
            new MessageEmbed.Field("Username", member.getUser().getName(), true),
            new MessageEmbed.Field("User ID", member.getUser().getId(), true),
            new MessageEmbed.Field("Joined Server", joinedDate.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(joinedDate) + "*", true),
            new MessageEmbed.Field("Joined Discord", createdDate.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(createdDate) + "*", true)
        ).setThumbnail(member.getUser().getEffectiveAvatarUrl());

        String memberRoles = "*This user is not in any roles*";
        if (!member.getRoles().isEmpty()) {
            memberRoles = member.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining("\n"));
        }

        placeholderMessage.addField(new MessageEmbed.Field(String.format("Roles (%s)", member.getRoles().size()), memberRoles, true));
        placeholderMessage.addField(new MessageEmbed.Field("Servers", NumberUtil.formatNicely(avaire.getMutualGuilds(member.getUser()).size()) + " the bot knows about", true));

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
