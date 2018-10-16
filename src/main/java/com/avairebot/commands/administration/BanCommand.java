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
import com.avairebot.contracts.commands.BanableCommand;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BanCommand extends BanableCommand {

    public BanCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Ban Command";
    }

    @Override
    public String getDescription() {
        return "Bans the mentioned user from the server with the provided reason, all messages the user has sent in the last 7 days will also be deleted in the process, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:commands <user> [reason]` - Bans the mentioned user with the given reason.",
            "`:commands <user id> [reason]` - Bans the user with given ID and for the given reason."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:commands @Senither Spam and acting like a twat`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(SoftBanCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ban");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,general.ban_members");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        return ban(avaire, this, context, args, false);
    }
}
