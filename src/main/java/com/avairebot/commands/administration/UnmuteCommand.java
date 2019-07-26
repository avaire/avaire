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
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnmuteCommand extends MuteableCommand {

    public UnmuteCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Unmute Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        return String.format(
            "Unmutes the mentioned user by removing the %s role from them, this action will be reported to any channel that has modloging enabled.",
            getMuteRoleNameFromContext(context)
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user> [reason]` - Unmutes the given user."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command @Senither` - Unmutes the user with no reason given.",
            "`:command @Senither Calmed down` - Unmutes the user with the given reason."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            MuteCommand.class,
            MuteRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("unmute");
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
            return sendErrorMessage(context, "This command requires a modlog channel to be set, a modlog channel can be set using the `{0}modlog` command.", prefix);
        }

        if (transformer.getMuteRole() == null) {
            String prefix = generateCommandPrefix(context.getMessage());
            return sendErrorMessage(context, "No mute role have been setup for the server, you can setup a mute role using the `{0}muterole` command.", prefix);
        }

        Role muteRole = context.getGuild().getRoleById(transformer.getMuteRole());
        if (muteRole == null) {
            String prefix = generateCommandPrefix(context.getMessage());
            return sendErrorMessage(context, "No mute role have been setup for the server, you can setup a mute role using the `{0}muterole` command.", prefix);
        }

        if (!context.getGuild().getSelfMember().canInteract(muteRole)) {
            return sendErrorMessage(context, "The {0} role used for mutes are positioned higher in the role hierarchy than any roles I have, so I can't automatically assign the role to other users, please fix this or use another role for mutes.", muteRole.getAsMention());
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "user");
        }

        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, "Invalid user mentioned, you must mention a user on the server you want to unmute to use this command.");
        }

        if (!RoleUtil.hasRole(context.getGuild().getMember(user), muteRole)) {
            return sendErrorMessage(context, "{0} doesn't appear to have the mute role, they may already have been unmuted!", user.getAsMention());
        }

        String reason = generateMessage(Arrays.copyOfRange(args, 1, args.length));
        context.getGuild().getController().removeSingleRoleFromMember(
            context.getGuild().getMember(user), muteRole
        ).reason(reason).queue(aVoid -> {
            ModlogAction modlogAction = new ModlogAction(
                ModlogType.UNMUTE, context.getAuthor(), user, reason
            );

            String caseId = Modlog.log(avaire, context, modlogAction);
            Modlog.notifyUser(user, context.getGuild(), modlogAction, caseId);

            try {
                avaire.getMuteManger().unregisterMute(context.getGuild().getIdLong(), user.getIdLong());

                context.makeSuccess(":target has been unmuted!")
                    .set("target", user.getAsMention())
                    .queue();
            } catch (SQLException e) {
                AvaIre.getLogger().error(e.getMessage(), e);
                context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
            }
        });

        return true;
    }

    private String generateMessage(String[] args) {
        return args.length == 0 ?
            "No reason was given." :
            String.join(" ", args);
    }
}
