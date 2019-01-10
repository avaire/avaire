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

package com.avairebot.commands.search;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.UrbanDictionaryService;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.StringReplacementUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrbanDictionaryCommand extends Command {

    private static final Pattern markdownUrlRegEx = Pattern.compile("\\[[A-Za-z0-9\\s]+\\]", Pattern.MULTILINE);

    public UrbanDictionaryCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Urban Dictionary Command";
    }

    @Override
    public String getDescription() {
        return "Get the definition of a word or sentence from [urbandictionary.com](https://www.urbandictionary.com/).";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <word or sentence>` - Gets the definition from Urban Dictionary");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command potato`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("urbandictionary", "urban", "define");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,4");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (context.isGuildMessage() && !context.getChannel().isNSFW()) {
            return sendErrorMessage(context, context.i18n("nsfwDisabled"));
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "word or sentence");
        }

        String word = String.join(" ", args);

        RequestFactory.makeGET("https://api.urbandictionary.com/v0/define")
            .addParameter("term", word)
            .send((Consumer<Response>) response -> {
                UrbanDictionaryService service = (UrbanDictionaryService) response.toService(UrbanDictionaryService.class);

                if (!service.hasData()) {
                    context.makeWarning(context.i18n("noResults"))
                        .set("query", String.join(" ", args))
                        .queue();
                    return;
                }

                UrbanDictionaryService.UrbanDictionary definition = service.getList().stream()
                    .sorted((d1, d2) -> {
                        int d1Score = buildScore(d1, word);
                        int d2Score = buildScore(d2, word);

                        if (d1Score == d2Score) {
                            return 0;
                        }
                        return d1Score > d2Score ? 1 : -1;
                    }).findFirst().get();

                double thumbsUp = definition.getThumbsUp();
                double thumbsDown = definition.getThumbsDown();
                double percentage = (thumbsUp / (thumbsUp + thumbsDown)) * 100;

                context.makeEmbeddedMessage(Color.decode("#1D2439"), formatUrlsInMessage(definition.getDefinition()))
                    .setTitle(
                        definition.getWord().trim().length() == 0
                            ? context.i18n("untitled") : definition.getWord(),
                        definition.getPermalink()
                    )
                    .addField(context.i18n("example"), formatUrlsInMessage(definition.getExample()), false)
                    .setFooter(
                        context.i18n("results")
                            .replace(":percentage", NumberUtil.formatNicelyWithDecimals(percentage) + "%")
                            .replace(":up", NumberUtil.formatNicely(definition.getThumbsUp()))
                            .replace(":down", NumberUtil.formatNicely(definition.getThumbsDown()))
                    ).queue();
            });
        return true;
    }

    private String formatUrlsInMessage(String message) {
        if (message == null) {
            return "";
        }

        Matcher matcher = markdownUrlRegEx.matcher(message);
        while (matcher.find()) {
            String term = matcher.group(0);
            String rawTerm = term.substring(1, term.length() - 1).replace(" ", "+");
            message = StringReplacementUtil
                .replaceAll(message, term, term + "(https://www.urbandictionary.com/define.php?term=" + rawTerm + ")");
        }

        return message;
    }

    private int buildScore(UrbanDictionaryService.UrbanDictionary definition, String originalWord) {
        try {
            return (definition.getThumbsUp() / (definition.getThumbsUp() + definition.getThumbsDown()) * 100)
                * (definition.getWord().equalsIgnoreCase(originalWord) ? 2 : 1);
        } catch (ArithmeticException ignored) {
            return 0;
        }
    }
}
