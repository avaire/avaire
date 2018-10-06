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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PartnerCommand extends SystemCommand {

    public PartnerCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Partner Command";
    }

    @Override
    public String getDescription() {
        return "Allows a bot admin to change the partnership a server has with the bot, servers who are partnered with the bot has less restrictions and more command slots(Like aliases, self-assignable roles, level roles, playlists, etc), if only the server ID is given the current partnership status will be displayed for the server with the given ID instead. ";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <server ID>` - Displays the current guild partnership status.",
            "`:command <server ID> <on|off>` - Toggles partnership on/off for the given server."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 284083636368834561` - Displays the servers partnerships status.",
            "`:command 284083636368834561 enable` - Makes the server a partner with Ava."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("partner");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "server ID");
        }

        if (!NumberUtil.isNumeric(args[0])) {
            return sendErrorMessage(context, "Invalid server ID given, `{0}` is not a valid server ID", args[0]);
        }

        Guild guild = avaire.getShardManager().getGuildById(args[0]);
        if (guild == null) {
            return sendErrorMessage(context, "The bot does not share any server with an ID of `{0}`", args[0]);
        }

        if (guild.getRegion().isVip()) {
            context.makeInfo("The **:server** server is a Discord partner, they will always be partnered with the bot because of that.")
                .set("server", guild.getName())
                .queue();

            return true;
        }

        try {
            Collection row = avaire.getDatabase()
                .newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .select("partner")
                .where("id", guild.getIdLong())
                .get();

            boolean isPartnered = !row.isEmpty() && row.first().getBoolean("partner", false);

            if (args.length == 1) {
                context.makeInfo("The **:server** server is currently **:status** with the bot.")
                    .set("server", guild.getName())
                    .set("status", isPartnered ? "partnered" : "not partnered")
                    .queue();

                return true;
            }

            ComparatorUtil.ComparatorType type = ComparatorUtil.getFuzzyType(args[1]);
            if (type.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
                return sendErrorMessage(context, "Invalid type given, you must either parse `enable` or `disable` to change the servers partnership status.");
            }

            if (isPartnered != type.getValue()) {
                avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", guild.getIdLong())
                    .update(statement -> statement.set("partner", type.getValue() ? 1 : 0));

                GuildController.forgetCache(context.getGuild().getIdLong());
            }

            context.makeSuccess("Successfully updated the **:server** servers partnership status.\nThe server is now **:status**")
                .set("server", guild.getName())
                .set("status", type.getValue() ? "partnered" : "not partnered")
                .queue();

            return true;
        } catch (SQLException e) {
            return sendErrorMessage(context, "Failed loading the guild data from the database, error: " + e.getMessage());
        }
    }
}
