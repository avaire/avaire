package com.avairebot.modules;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
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
     * @param context The message context object for the current message.
     * @param args    The arguments given by the user who ran the command.
     * @param soft    Determines if the user should be softbanned or not.
     * @return True if the user was banned successfully, false otherwise.
     */
    public static boolean ban(AvaIre avaire, Command command, CommandMessage context, String[] args, boolean soft) {
        if (context.getMentionedUsers().isEmpty() || !userRegEX.matcher(args[0]).matches()) {
            return command.sendErrorMessage(context, "You must mention the user you want to ban.");
        }

        User user = context.getMentionedUsers().get(0);
        if (userHasHigherRole(user, context.getMember())) {
            return command.sendErrorMessage(context, "You can't ban people with a higher, or the same role as yourself.");
        }

        return banUser(avaire, context, user, args, soft);
    }

    private static boolean banUser(AvaIre avaire, CommandMessage context, User user, String[] args, boolean soft) {
        String reason = generateMessage(args);

        context.getGuild().getController().ban(user, soft ? 0 : 7, String.format("%s - %s#%s (%s)",
            reason,
            context.getAuthor().getName(),
            context.getAuthor().getDiscriminator(),
            context.getAuthor().getId()
        )).queue(aVoid -> {
            ModlogModule.log(avaire, context, new ModlogModule.ModlogAction(
                    soft ? ModlogModule.ModlogType.SOFT_BAN : ModlogModule.ModlogType.BAN,
                    context.getAuthor(), user, reason
                )
            );

            context.makeSuccess("**:target** was permanently banned by :user for \":reason\"")
                .set("target", user.getName() + "#" + user.getDiscriminator())
                .set("reason", reason)
                .queue();
        }, throwable -> context.makeWarning("Failed to ban **:target** due to an error: :error")
            .set("target", user.getName() + "#" + user.getDiscriminator())
            .set("error", throwable.getMessage())
            .queue());

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
