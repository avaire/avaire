package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BanCommand extends Command {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    public BanCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Ban Command";
    }

    @Override
    public String getDescription() {
        return "Bans the mentioned user from the server with the provided reason, all messages the user has sent in the last 7 days will also be deleted in the process, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user> [reason]` - Bans the mentioned user with the given reason.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command @Senither Spam and acting like a twat`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ban");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,general.ban_members");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (message.getMentionedUsers().isEmpty() || !userRegEX.matcher(args[0]).matches()) {
            return sendErrorMessage(message, "You must mention the user you want to ban.");
        }

        User user = message.getMentionedUsers().get(0);
        if (userHasHigherRole(user, message.getMember())) {
            return sendErrorMessage(message, "You can't ban people with a higher, or the same role as yourself.");
        }

        return banUser(message, user, args);
    }

    private boolean banUser(Message message, User user, String[] args) {
        String reason = generateMessage(args);
        message.getGuild().getController().ban(user, 7, reason).queue(aVoid -> {
            MessageFactory.makeSuccess(message, "**%s** was permanently banned by <@%s> for \"%s\"",
                user.getName() + "#" + user.getDiscriminator(),
                message.getAuthor().getId(),
                reason
            ).queue();
        }, throwable -> MessageFactory.makeWarning(message, "Failed to ban **%s** due to an error: %s",
            user.getName() + "#" + user.getDiscriminator(),
            throwable.getMessage()
        ).queue());

        return true;
    }

    private boolean userHasHigherRole(User user, Member author) {
        Role role = RoleUtil.getHighestFrom(author.getGuild().getMember(user));
        return role != null && RoleUtil.isRoleHierarchyHigher(author.getRoles(), role);
    }

    private String generateMessage(String[] args) {
        return args.length < 2 ?
            "No reason was given." :
            String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
