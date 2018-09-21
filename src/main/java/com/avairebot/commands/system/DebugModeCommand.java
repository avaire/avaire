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
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.utilities.ComparatorUtil;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.Collections;
import java.util.List;

public class DebugModeCommand extends SystemCommand {

    public DebugModeCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Debug Mode Command";
    }

    @Override
    public String getDescription() {
        return "Toggles debug mode on/off during runtime, this will enable passing the context between rest actions to give a better debug result, and to make debugging with Sentry more information.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <on|off>` - Toggles debug mode on or off");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command on` - Enables debug mode.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("debug-mode");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "You must either provide `on` or `off` to either enable or disable debugging mode.");
        }

        ComparatorUtil.ComparatorType type = ComparatorUtil.getFuzzyType(args[0]);
        RestAction.setPassContext(type.getValue());

        context.makeSuccess("Debug mode has been **:status**")
            .set("status", type.getValue() ? "Enabled" : "Disabled")
            .queue();

        return true;
    }
}
