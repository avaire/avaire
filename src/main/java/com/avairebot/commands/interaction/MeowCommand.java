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

package com.avairebot.commands.interaction;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class MeowCommand extends InteractionCommand {

    public MeowCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/PJUZAXX.gif",
            "https://i.imgur.com/WtVaAGp.gif",
            "https://i.imgur.com/E3JpP7e.gif",
            "https://i.imgur.com/wAD0tqK.gif",
            "https://i.imgur.com/W7qSt0G.gif",
            "https://i.imgur.com/PLwWhJz.gif",
            "https://i.imgur.com/ozn4UyC.gif",
            "https://i.imgur.com/DbvyqfH.gif",
            "https://i.imgur.com/WsLsRkd.gif",
            "https://i.imgur.com/fWLT1Q0.gif",
            "https://i.imgur.com/zyBznwg.gif"
        );
    }

    @Override
    public String getName() {
        return "Meow Command :3";
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIDDEN;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("meows", "meow");
    }
}
