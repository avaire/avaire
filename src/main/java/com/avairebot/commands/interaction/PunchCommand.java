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
import com.avairebot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.List;

public class PunchCommand extends InteractionCommand {

    public PunchCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/0baM7Wd.gif",
            "https://i.imgur.com/fVPGbo5.gif",
            "https://i.imgur.com/vHRVMe9.gif",
            "https://i.imgur.com/g3MH1Bs.gif",
            "https://i.imgur.com/7SbOwl4.gif"
        );
    }

    @Override
    public String getName() {
        return "Punch Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("punch", "punches");
    }
}
