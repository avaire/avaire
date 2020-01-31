/*
 * Copyright (c) 2020.
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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class LaughCommand extends InteractionCommand {

    public LaughCommand(AvaIre avaire) {
        super(avaire);
    }

    @Nonnull
    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/x4tmoDG.gif",
            "https://i.imgur.com/SQurrBn.gif",
            "https://i.imgur.com/ztrDzB9.gif",
            "https://i.imgur.com/zq0RN4l.gif",
            "https://i.imgur.com/4LOL35L.gif",
            "https://i.imgur.com/Lcm7Zkx.gif",
            "https://i.imgur.com/X1oVvk4.gif",
            "https://i.imgur.com/WJVT6J0.gif"
        );
    }

    @Override
    public String getName() {
        return "Laugh Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("laugh", "lol", "lul");
    }
}
