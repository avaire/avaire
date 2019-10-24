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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.Category;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.middleware.global.IsCategoryEnabled;
import com.avairebot.utilities.ComparatorUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlobalToggleCategoryCommand extends SystemCommand {

    public GlobalToggleCategoryCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Toggle Command Module";
    }

    @Override
    public String getDescription() {
        return "Toggle a command category on or off for all servers the bot is in.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <category> <status> [message]` - Toggels the given command category on or off globally."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command music off Stuff is broke yo` - Disables the music module globally with the given message."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("global-toggle", "gtc");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "category");
        }

        Category category = CategoryHandler.fromLazyName(args[0]);
        if (category == null) {
            return sendErrorMessage(context, "Invalid category name given, found no command categories called `{0}`", args[0]);
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "status");
        }

        ComparatorUtil.ComparatorType type = ComparatorUtil.getFuzzyType(args[1]);
        if (type.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
            return sendErrorMessage(context, "errors.invalidProperty", "status");
        }

        if (type.getValue()) {
            IsCategoryEnabled.enableCategory(category);
        } else {
            IsCategoryEnabled.disableCategory(category,
                args.length > 2
                    ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                    : null
            );
        }

        context.makeInfo("The :category command category have successfully been :status globally!")
            .set("category", category.getName())
            .set("status", type.getValue() ? "enabled" : "disabled")
            .queue();

        return true;
    }
}
