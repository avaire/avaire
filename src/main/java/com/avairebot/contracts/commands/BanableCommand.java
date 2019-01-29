/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.modlog.Modlog;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
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
        if (user != null) {
            return banMemberOfServer(avaire, command, context, user, args, soft);
        }

        if (args.length > 0 && NumberUtil.isNumeric(args[0]) && args[0].length() > 16) {
            try {
                long userId = Long.parseLong(args[0], 10);

                Member member = context.getGuild().getMemberById(userId);
                if (member != null) {
                    return banMemberOfServer(avaire, command, context, member.getUser(), args, soft);
                }

                return banUserById(avaire, command, context, userId, args, soft);
            } catch (NumberFormatException ignored) {
                // This should never really be called since we check if
                // the argument is a number in the if-statement above.
            }
        }
        return command.sendErrorMessage(context, context.i18n("mustMentionUser"));
    }

    private boolean banUserById(AvaIre avaire, Command command, CommandMessage context, long userId, String[] args, boolean soft) {
        String reason = generateReason(args);

        context.getGuild().getController().ban(String.valueOf(userId), soft ? 0 : 7, String.format("%s - %s#%s (%s)",
            reason,
            context.getAuthor().getName(),
            context.getAuthor().getDiscriminator(),
            context.getAuthor().getId()
        )).queue(aVoid -> {
            User user = avaire.getShardManager().getUserById(userId);

            if (user != null) {
                Modlog.log(avaire, context, new ModlogAction(
                    soft ? ModlogType.SOFT_BAN : ModlogType.BAN,
                    context.getAuthor(), user, reason
                ));
            } else {
                Modlog.log(avaire, context, new ModlogAction(
                    soft ? ModlogType.SOFT_BAN : ModlogType.BAN,
                    context.getAuthor(), userId, reason
                ));
            }

            context.makeSuccess(context.i18n("success"))
                .set("target", userId)
                .set("reason", reason)
                .queue(ignoreMessage -> context.delete().queue(null, RestActionUtil.ignore));
        }, throwable -> context.makeWarning(context.i18n("failedToBan"))
            .set("target", userId)
            .set("error", throwable.getMessage())
            .queue());

        return true;
    }

    private boolean banMemberOfServer(AvaIre avaire, Command command, CommandMessage context, User user, String[] args, boolean soft) {
        if (userHasHigherRole(user, context.getMember())) {
            return command.sendErrorMessage(context, context.i18n("higherRole"));
        }

        if (!context.getGuild().getSelfMember().canInteract(context.getGuild().getMember(user))) {
            return sendErrorMessage(context, context.i18n("userHaveHigherRole", user.getAsMention()));
        }

        String reason = generateReason(args);

        ModlogAction modlogAction = new ModlogAction(
            soft ? ModlogType.SOFT_BAN : ModlogType.BAN,
            context.getAuthor(), user, reason
        );

        String caseId = Modlog.log(avaire, context, modlogAction);

        Modlog.notifyUser(user, context.getGuild(), modlogAction, caseId);

        context.getGuild().getController().ban(user, soft ? 0 : 7, String.format("%s - %s#%s (%s)",
            reason,
            context.getAuthor().getName(),
            context.getAuthor().getDiscriminator(),
            context.getAuthor().getId()
        )).queue(aVoid -> {
            context.makeSuccess(context.i18n("success"))
                .set("target", user.getName() + "#" + user.getDiscriminator())
                .set("reason", reason)
                .queue();
        }, throwable -> context.makeWarning(context.i18n("failedToBan"))
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
