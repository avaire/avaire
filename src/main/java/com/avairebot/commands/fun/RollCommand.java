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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RollCommand extends Command {

    public RollCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Roll Command";
    }

    @Override
    public String getDescription() {
        return "Roll a random number between 1 and 100, or within the given parameters.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command`",
            "`:command [max]`",
            "`:command [min] [max]`"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 3 6`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("roll");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        double min = 1;
        double max = 100;

        try {
            if (args.length == 1) {
                max = parseArgument(context, args[0], "max");
            } else if (args.length > 1) {
                min = parseArgument(context, args[0], "min");
                max = parseArgument(context, args[1], "max");
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        min = Math.ceil(min);
        max = Math.floor(max);

        double random = Math.floor(Math.random() * (max - min + 1)) + min;

        context.makeInfo(context.i18n("rolledNumber"))
            .set("number", (int) random)
            .set("min", (int) min)
            .set("max", (int) max)
            .queue();

        return true;
    }

    private double parseArgument(CommandMessage context, String argument, String error) throws NumberFormatException {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException ex) {
            sendErrorMessage(context, context.i18n("invalidValueGiven"), error, error);
            throw ex;
        }
    }
}
