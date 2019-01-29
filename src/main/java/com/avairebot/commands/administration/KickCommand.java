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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.modlog.Modlog;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RestActionUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "kick-command")
public class KickCommand extends Command {

    public KickCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Kick Command";
    }

    @Override
    public String getDescription() {
        return "Kicks the mentioned user from the server with the provided reason, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user> [reason]` - Kicks the mentioned user with the given reason.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Senither Spamming things`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(VoiceKickCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("kick");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:all,general.kick_members",
            "throttle:user,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, context.i18n("mustMentionUser"));
        }

        if (userHasHigherRole(user, context.getMember())) {
            return sendErrorMessage(context, context.i18n("higherOrSameRole"));
        }

        if (!context.getGuild().getSelfMember().canInteract(context.getGuild().getMember(user))) {
            return sendErrorMessage(context, context.i18n("cantKickUser", user.getAsMention()));
        }

        return kickUser(context, context.getGuild().getMember(user), args);
    }

    private boolean kickUser(CommandMessage context, Member user, String[] args) {
        String reason = generateMessage(args);

        ModlogAction modlogAction = new ModlogAction(
            ModlogType.KICK,
            context.getAuthor(),
            user.getUser(),
            reason
        );

        String caseId = Modlog.log(avaire, context, modlogAction);

        Modlog.notifyUser(user.getUser(), context.getGuild(), modlogAction, caseId);

        context.getGuild().getController().kick(user, String.format("%s - %s#%s (%s)",
            reason,
            context.getAuthor().getName(),
            context.getAuthor().getDiscriminator(),
            context.getAuthor().getId()
        )).queue(aVoid -> {
                context.makeSuccess(context.i18n("success"))
                    .set("target", user.getUser().getName() + "#" + user.getUser().getDiscriminator())
                    .set("reason", reason)
                    .queue(ignoreMessage -> context.delete().queue(null, RestActionUtil.ignore));
            }, throwable -> context.makeWarning(context.i18n("error"))
                .set("target", user.getUser().getName() + "#" + user.getUser().getDiscriminator())
                .set("error", throwable.getMessage())
                .queue()
        );
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
