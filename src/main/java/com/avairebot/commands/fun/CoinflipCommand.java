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
import com.avairebot.utilities.RandomUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class CoinflipCommand extends Command {

    private final String heads = "https://cdn.discordapp.com/attachments/279462105277530112/279614727406223360/Heads.png";
    private final String tails = "https://cdn.discordapp.com/attachments/279462105277530112/279614727431258112/Tails.png";

    public CoinflipCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Coinflip Command";
    }

    @Override
    public String getDescription() {
        return "Flips a coin heads or tails.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Flips a coin for either heads or tails.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("coinflip", "coin");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        boolean isHeads = RandomUtil.getBoolean();

        context.makeEmbeddedMessage()
            .setTitle(context.i18n(isHeads ? "heads" : "tails"))
            .setImage(isHeads ? heads : tails)
            .queue();

        return true;
    }
}
