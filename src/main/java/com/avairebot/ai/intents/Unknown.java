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

package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.help.HelpCommand;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.utilities.StringReplacementUtil;

public class Unknown extends Intent {

    public Unknown(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "input.unknown";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onIntent(CommandMessage context, AIResponse response) {
        context.makeWarning(
            StringReplacementUtil.replaceAll(
                response.getResult().getFulfillment().getSpeech(),
                "!help", CommandHandler.getCommand(HelpCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            )
        ).queue();
    }
}
