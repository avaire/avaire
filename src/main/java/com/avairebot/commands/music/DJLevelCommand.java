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

package com.avairebot.commands.music;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DJLevelCommand extends Command {

    public DJLevelCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "DJ Level Command";
    }

    @Override
    public String getDescription() {
        return "Change the DJ level requirement for the server, this changes what music commands people can use with or without the `DJ` Discord role.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("djlevel");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current DJ Level for the server.",
            "`:command types` - Displays all the types and some info about them.",
            "`:command <type>` - Change the DJ Level to the given type."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command types` - Displays all the types and info about them.",
            "`:command normal` - Changes the DJ Level to \"normal\"."
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:guild,1,4"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_SETTINGS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (args.length == 0) {
            context.makeInfo(getLevelInformation(context, transformer.getDJLevel()))
                .setTitle(context.i18n("title", transformer.getDJLevel().getName()))
                .setFooter(context.i18n("footer", generateCommandTrigger(context.getMessage())))
                .queue();
            return true;
        }

        if (args[0].equalsIgnoreCase("type") || args[0].equalsIgnoreCase("types")) {
            PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(context.getChannel());

            for (DJGuildLevel level : DJGuildLevel.values()) {
                placeholderMessage.addField(level.getName(), getLevelInformation(context, level), false);
            }

            placeholderMessage
                .setTitle(context.i18n("types.title"))
                .setFooter(context.i18n("types.footer", generateCommandTrigger(context.getMessage())))
                .queue();

            return true;
        }

        DJGuildLevel level = DJGuildLevel.fromName(args[0]);
        if (level == null) {
            return sendErrorMessage(context, context.i18n("invalidType",
                args[0], String.join("`, `", DJGuildLevel.getNames())
            ));
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("dj_level", level.getId()));
            transformer.setDJLevel(level);

            context.makeSuccess("The `DJ Level` status has changed to **:type**.\n:info")
                .set("type", level.getName())
                .set("info", getLevelInformation(context, level))
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
            AvaIre.getLogger().error(e.getMessage(), e);
        }

        return false;
    }

    private String getLevelInformation(CommandMessage context, DJGuildLevel level) {
        return context.i18n("information." + level.name().toLowerCase());
    }
}
