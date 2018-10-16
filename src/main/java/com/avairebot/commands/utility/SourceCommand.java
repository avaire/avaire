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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SourceCommand extends Command {

    private final String rootUrl = "https://github.com/avaire/avaire";

    public SourceCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Source Command";
    }

    @Override
    public String getDescription() {
        return "Gives you the source code for the Bot, or the code for a given commands.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:commands` - Returns the full source code for the bot.",
            "`:commands <commands>` - Returns the source code for the given commands."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:commands ping`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("source");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            context.makeInfo(context.i18n("noArgs") + "\n\n" + rootUrl).queue();
            return true;
        }

        CommandContainer command = getCommand(context.getMessage(), args[0]);
        if (command == null) {
            context.makeInfo(context.i18n("invalidCommand") + "\n\n" + rootUrl).queue();
            return true;
        }

        String sourceUri = command.getSourceUri();
        if (sourceUri == null) {
            context.makeInfo(context.i18n("externalCommand") + "\n\n" + rootUrl).queue();
            return true;
        }

        context.makeInfo(context.i18n("commands") + "\n\n" + sourceUri)
            .set("commands", command.getCommand().getName())
            .queue();

        return true;
    }

    private CommandContainer getCommand(Message message, String commandString) {
        CommandContainer command = CommandHandler.getCommand(message, commandString);
        if (command != null) {
            return command;
        }
        return CommandHandler.getLazyCommand(commandString);
    }
}
