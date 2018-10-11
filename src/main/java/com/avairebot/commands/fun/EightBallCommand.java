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
import com.avairebot.language.I18n;
import com.avairebot.utilities.RandomUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EightBallCommand extends Command {

    private final List<String> answers = Arrays.asList(
        "It is certain",
        "It is decidedly so",
        "Without a doubt",
        "Yes definitely",
        "You may rely on it",
        "As I see it, yes",
        "Most likely",
        "Outlook good",
        "Yes",
        "Signs point to yes",
        "Reply hazy try again",
        "Ask again later",
        "Better not tell you now",
        "Cannot predict now",
        "Concentrate and ask again",
        "Don't count on it",
        "My reply is no",
        "My sources say no",
        "Outlook not so good",
        "Very doubtful"
    );

    public EightBallCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Eight Ball Command";
    }

    @Override
    public String getDescription() {
        return "Ask 8Ball a question and get a random response back.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <question>`");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command will i have a good day today?`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("8ball");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("mustIncludeQuestion"));
        }

        String answers = context.getI18nCommandPrefix() + ".answers";

        // Loads the answers from the selected i18n file
        if (context.getI18n().contains(answers) && context.getI18n().isList(answers)) {
            return sendAnswerFromList(context, context.getI18n().getStringList(answers));
        }

        // Tries to load the answers from the default i18n file instead
        if (I18n.getDefaultLanguage().getConfig().contains(answers) && I18n.getDefaultLanguage().getConfig().isList(answers)) {
            return sendAnswerFromList(context, I18n.getDefaultLanguage().getConfig().getStringList(answers));
        }

        // Sends the default list
        return sendAnswerFromList(context, this.answers);
    }

    private boolean sendAnswerFromList(CommandMessage context, List<String> answers) {
        context.makeEmbeddedMessage(
            Color.decode("#2A2C31"), (String) RandomUtil.pickRandom(answers)
        ).queue();

        return true;
    }
}
