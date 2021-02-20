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
import java.util.Collections;
import java.util.List;

public class HandHoldCommand extends InteractionCommand {

    public HandHoldCommand(AvaIre avaire) {
        super(avaire);
    }

    @Nonnull
    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://imgur.com/fk8RpWF.gif",
            "https://imgur.com/4p4afJU.gif",
            "https://imgur.com/O9Zi3TB.gif",
            "https://imgur.com/SHY2BPh.gif"
        );
    }

    @Override
    public String getName() {
        return "Hand Hold Command";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("handhold");
    }
}
