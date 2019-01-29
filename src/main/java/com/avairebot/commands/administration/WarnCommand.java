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
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.modlog.Modlog;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarnCommand extends Command {

    public WarnCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Warn Command";
    }

    @Override
    public String getDescription() {
        return "Warns a given user with a message, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user> [reason]` - Warns the mentioned user with the given reason."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command @Senither Being a potato` - Warns Senither for being a potato."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ModlogHistoryCommand.class,
            ModlogReasonCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("warn");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,text.manage_messages",
            "throttle:channel,1,5"
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
            return sendErrorMessage(context, context.i18n("requiresModlogIsEnabled", prefix));
        }

        User user = null;
        if (args.length > 0) {
            user = MentionableUtil.getUser(context, args);
        }

        if (user == null) {
            return sendErrorMessage(context, context.i18n("mustMentionUse"));
        }

        if (user.isBot()) {
            return sendErrorMessage(context, context.i18n("warnBots"));
        }

        String reason = "No reason was given.";
        if (args.length > 1) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        ModlogAction modlogAction = new ModlogAction(
            ModlogType.WARN,
            context.getAuthor(), user,
            reason
        );

        String caseId = Modlog.log(avaire, context.getGuild(), transformer, modlogAction);

        if (caseId == null) {
            return sendErrorMessage(context, context.i18n("failedToLogWarning"));
        }

        Modlog.notifyUser(user, context.getGuild(), modlogAction, caseId);

        context.makeWarning(context.i18n("message"))
            .set("target", user.getName() + "#" + user.getDiscriminator())
            .set("reason", reason)
            .setFooter("Case ID #" + caseId)
            .setTimestamp(Instant.now())
            .queue(ignoreMessage -> context.delete().queue(null, RestActionUtil.ignore), RestActionUtil.ignore);

        return true;
    }
}
