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

public class BiteCommand extends InteractionCommand {

    public BiteCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/qcPNLOH.gif",
            "https://i.imgur.com/t89dF2n.gif",
            "https://i.imgur.com/fPCg6Or.gif",
            "https://i.imgur.com/NWBokoF.gif",
            "https://i.imgur.com/h0FfIKf.gif",
            "https://i.imgur.com/dnOBA0s.gif"
        );
    }

    @Override
    public String getName() {
        return "Bite Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("bite", "bites");
    }
}
