/*
 * Copyright (c) 2019.
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
import com.avairebot.contracts.commands.*;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.modlog.Modlog;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteCommand extends MuteableCommand {

    private final Pattern timeRegEx = Pattern.compile("([0-9]+[w|d|h|m|s])");

    public MuteCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Mute Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        return String.format(
            "Mutes the mentioned user by giving them the %s role, if a time is specified the user will automatically be unmuted again after the time has elapsed, this action will be reported to any channel that has modloging enabled.",
            getMuteRoleNameFromContext(context)
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user> [time] [reason]` - Mutes the mentioned user with the given reason for the given amount of time."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command @Senither Spams too much` - Mutes the user permanently.",
            "`:command @Senither 30m Calm down` - Mutes the user for 30 minutes.",
            "`:command @Senither 1d` - Mutes the user for 1 day with no reason."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            UnmuteCommand.class,
            MuteRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("mute");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,text.manage_messages",
            "require:bot,general.manage_roles",
            "throttle:guild,1,4"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (transformer.getModlog() == null) {
            String prefix = generateCommandPrefix(context.getMessage());
            return sendErrorMessage(context, context.i18n("requiresModlogToBeSet", prefix));
        }

        if (transformer.getMuteRole() == null) {
            String prefix = generateCommandPrefix(context.getMessage());
            return sendErrorMessage(context, context.i18n("requireMuteRoleToBeSet", prefix));
        }

        Role muteRole = context.getGuild().getRoleById(transformer.getMuteRole());
        if (muteRole == null) {
            String prefix = generateCommandPrefix(context.getMessage());
            return sendErrorMessage(context, context.i18n("requireMuteRoleToBeSet", prefix));
        }

        if (!context.getGuild().getSelfMember().canInteract(muteRole)) {
            return sendErrorMessage(context, context.i18n("muteRoleIsPositionedHigher", muteRole.getAsMention()));
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "user");
        }

        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, context.i18n("invalidUserMentioned"));
        }

        if (userHasHigherRole(user, context.getMember())) {
            return sendErrorMessage(context, context.i18n("higherOrSameRole"));
        }

        Carbon expiresAt = null;
        if (args.length > 1) {
            expiresAt = parseTime(args[1]);
        }

        if (expiresAt != null && expiresAt.copy().subSeconds(61).isPast()) {
            return sendErrorMessage(context, context.i18n("invalidTimeGiven"));
        }

        String reason = generateMessage(Arrays.copyOfRange(args, expiresAt == null ? 1 : 2, args.length));
        ModlogType type = expiresAt == null ? ModlogType.MUTE : ModlogType.TEMP_MUTE;

        final Carbon finalExpiresAt = expiresAt;
        context.getGuild().getController().addRolesToMember(
            context.getGuild().getMember(user), muteRole
        ).reason(reason).queue(aVoid -> {
            ModlogAction modlogAction = new ModlogAction(
                type, context.getAuthor(), user,
                finalExpiresAt != null
                    ? finalExpiresAt.toDayDateTimeString() + " (" + finalExpiresAt.diffForHumans(true) + ")" + "\n" + reason
                    : "\n" + reason
            );

            String caseId = Modlog.log(avaire, context, modlogAction);
            Modlog.notifyUser(user, context.getGuild(), modlogAction, caseId);

            try {
                avaire.getMuteManger().registerMute(caseId, context.getGuild().getIdLong(), user.getIdLong(), finalExpiresAt);

                context.makeSuccess(context.i18n("userHasBeenMuted"))
                    .set("target", user.getAsMention())
                    .set("time", finalExpiresAt == null
                        ? context.i18n("time.permanently")
                        : context.i18n("time.forFormat", finalExpiresAt.diffForHumans(true)))
                    .queue();
            } catch (SQLException e) {
                AvaIre.getLogger().error(e.getMessage(), e);
                context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
            }
        });

        return true;
    }

    private boolean userHasHigherRole(User user, Member author) {
        Role role = RoleUtil.getHighestFrom(author.getGuild().getMember(user));
        return role != null && RoleUtil.isRoleHierarchyHigher(author.getRoles(), role);
    }

    private Carbon parseTime(String string) {
        Matcher matcher = timeRegEx.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        Carbon time = Carbon.now().addSecond();
        do {
            String group = matcher.group();

            String type = group.substring(group.length() - 1, group.length());
            int timeToAdd = NumberUtil.parseInt(group.substring(0, group.length() - 1));

            switch (type.toLowerCase()) {
                case "w":
                    time.addWeeks(timeToAdd);
                    break;

                case "d":
                    time.addDays(timeToAdd);
                    break;

                case "h":
                    time.addHours(timeToAdd);
                    break;

                case "m":
                    time.addMinutes(timeToAdd);
                    break;

                case "s":
                    time.addSeconds(timeToAdd);
                    break;
            }
        } while (matcher.find());

        return time;
    }

    private String generateMessage(String[] args) {
        return args.length == 0 ?
            "No reason was given." :
            String.join(" ", args);
    }
}
