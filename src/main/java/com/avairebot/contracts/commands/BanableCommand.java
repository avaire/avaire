package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogModule;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;

public abstract class BanableCommand extends Command {
    
    /**
     * Creates the given command instance by calling {@link Command#Command(AvaIre, boolean)} with allowDM set to true.
     *
     * @param avaire The AvaIre class instance.
     */
    public BanableCommand(AvaIre avaire) {
        super(avaire);
    }

    /**
     * Creates the given command instance with the given
     * AvaIre instance and the allowDM settings.
     *
     * @param avaire  The AvaIre class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public BanableCommand(AvaIre avaire, boolean allowDM) {
        super(avaire, allowDM);
    }

    /**
     * Bans the mentioned user from the current server is a valid user was given.
     *
     * @param command The command that was used in banning the user.
     * @param context The message context object for the current message.
     * @param args    The arguments given by the user who ran the command.
     * @param soft    Determines if the user should be softbanned or not.
     * @return True if the user was banned successfully, false otherwise.
     */
    protected boolean ban(AvaIre avaire, Command command, CommandMessage context, String[] args, boolean soft) {
        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return command.sendErrorMessage(context, "You must mention the user you want to ban.");
        }

        if (userHasHigherRole(user, context.getMember())) {
            return command.sendErrorMessage(context, "You can't ban people with a higher, or the same role as yourself.");
        }

        if (!context.getGuild().getSelfMember().canInteract(context.getGuild().getMember(user))) {
            return sendErrorMessage(context, "I can't ban {0}, they have a higher role than me, if you want be to be able to ban the user, please reajust my role position to above {0} highest role.",
                user.getAsMention()
            );
        }

        return banUser(avaire, context, user, args, soft);
    }

    private boolean banUser(AvaIre avaire, CommandMessage context, User user, String[] args, boolean soft) {
        String reason = generateReason(args);

        context.getGuild().getController().ban(user, soft ? 0 : 7, String.format("%s - %s#%s (%s)",
            reason,
            context.getAuthor().getName(),
            context.getAuthor().getDiscriminator(),
            context.getAuthor().getId()
        )).queue(aVoid -> {
            ModlogModule.log(avaire, context, new ModlogAction(
                    soft ? ModlogType.SOFT_BAN : ModlogType.BAN,
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

    private boolean userHasHigherRole(User user, Member author) {
        Role role = RoleUtil.getHighestFrom(author.getGuild().getMember(user));
        return role != null && RoleUtil.isRoleHierarchyHigher(author.getRoles(), role);
    }

    private String generateReason(String[] args) {
        return args.length < 2 ?
            "No reason was given." :
            String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
