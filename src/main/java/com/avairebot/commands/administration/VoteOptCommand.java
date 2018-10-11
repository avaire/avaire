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
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VoteOptCommand extends Command {

    public VoteOptCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Vote Opt Command";
    }

    @Override
    public String getDescription() {
        return "Opt in or out of getting vote messages when you vote for Ava.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <in|out>` - Opt in/out of vote messages when you vote");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command in` - Enables vote messages when you vote.",
            "`:command out` - Disables vote messages when you vote."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("voteopt");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "isDMMessage", "throttle:user,1,10"
        );
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIDDEN;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("invalidArgument"));
        }

        int opt = getOptValue(args[0]);
        if (opt == -1) {
            return sendErrorMessage(context, context.i18n("invalidArgument"));
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .useAsync(true)
                .where("user_id", context.getAuthor().getIdLong())
                .update(statement -> statement.set("opt_in", opt));
        } catch (SQLException ignored) {
        }

        context.makeSuccess(context.i18n("message"))
            .set("type", context.i18n("type." + (opt == 0 ? "out" : "in")))
            .queue();

        avaire.getVoteManager().getVoteEntityWithFallback(context.getAuthor()).setOptIn(opt == 1);

        return true;
    }

    private int getOptValue(String arg) {
        if (arg.equalsIgnoreCase("in")) return 1;
        if (arg.equalsIgnoreCase("out")) return 0;
        return -1;
    }
}
