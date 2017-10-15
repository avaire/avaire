package com.avairebot.orion.modules;

import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.regex.Pattern;

public class BanModule {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    /**
     * Bans the mentioned user from the current server is a valid user was given.
     *
     * @param command The command that was used in banning the user.
     * @param message The message context object for the current message.
     * @param args    The arguments given by the user who ran the command.
     * @param soft    Determines if the user should be softbanned or not.
     * @return True if the user was banned successfully, false otherwise.
     */
    public static boolean ban(Command command, Message message, String[] args, boolean soft) {
        if (message.getMentionedUsers().isEmpty() || !userRegEX.matcher(args[0]).matches()) {
            return command.sendErrorMessage(message, "You must mention the user you want to ban.");
        }

        User user = message.getMentionedUsers().get(0);
        if (userHasHigherRole(user, message.getMember())) {
            return command.sendErrorMessage(message, "You can't ban people with a higher, or the same role as yourself.");
        }

        return banUser(message, user, args, soft);
    }

    private static boolean banUser(Message message, User user, String[] args, boolean soft) {
        String reason = generateMessage(args);
        message.getGuild().getController().ban(user, soft ? 0 : 7, reason).queue(aVoid -> {
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

    private static boolean userHasHigherRole(User user, Member author) {
        Role role = RoleUtil.getHighestFrom(author.getGuild().getMember(user));
        return role != null && RoleUtil.isRoleHierarchyHigher(author.getRoles(), role);
    }

    private static String generateMessage(String[] args) {
        return args.length < 2 ?
            "No reason was given." :
            String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
