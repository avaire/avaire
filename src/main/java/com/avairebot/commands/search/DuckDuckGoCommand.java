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
import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

public class DuckDuckGoCommand extends Command {

    private static final Map<String, String> HTTP_HEADERS = new HashMap<>();

    static {
        HTTP_HEADERS.put("Accept-Language", "en-US,en;q=0.8,en-GB;q=0.6,da;q=0.4");
        HTTP_HEADERS.put("Cache-Control", "no-cache, no-store, must-revalidate");
        HTTP_HEADERS.put("Pragma", "no-cache");
        HTTP_HEADERS.put("Expires", "0");
    }

    public DuckDuckGoCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "DuckDuckGo Command";
    }

    @Override
    public String getDescription() {
        return "Searches [DuckDuckGo.com](https://duckduckgo.com/) with the given query and returns the first six results, if the command is used in a channel with NSFW disabled, all NSFW search results will be removed from the results.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <query>` - Searchs DuckDuckGo for your query.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command AvaIre Bot`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("duckduckgo", "ddg", "g");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (context.isGuildMessage() && !context.getChannel().isNSFW()) {
            return sendErrorMessage(context, context.i18n("nsfwDisabled"));
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "query");
        }

        try {
            Map<String, String> headers = new HashMap<>();
            headers.putAll(HTTP_HEADERS);
            headers.put("User-Agent", "AvaIre-Discord-Bot (" + avaire.getSelfUser().getId() + ")");

            context.getMessageChannel().sendTyping().queue();

            boolean nsfwEnabled = isNSFWEnabled(context);
            Document document = Jsoup.connect(generateUri(args, nsfwEnabled))
                .headers(headers)
                .timeout(10000)
                .get();

            int results = 0;
            List<String> result = new ArrayList<>();
            Elements elements = document.select("div#links div.result");
            for (Element element : elements) {
                Elements link = element.select("h2.result__title a");
                if (isAdvertisement(link)) {
                    continue;
                }

                if (results == 0) {
                    result.add(prepareLinkElement(link) + "\n" + context.i18n("seeAlso"));
                    results++;
                    continue;
                }

                result.add(prepareLinkElement(link));
                results++;

                if (results > 5) {
                    break;
                }
            }

            PlaceholderMessage resultMessage = MessageFactory.makeEmbeddedMessage(context.getMessageChannel(), Color.decode("#DE5833"))
                .setDescription(String.join("\n", result))
                .setTitle(context.i18n("searchResults", String.join(" ", args)));

            if (result.isEmpty() || (result.size() == 1 && result.get(0).startsWith("-1&uddg"))) {
                resultMessage
                    .setColor(MessageType.WARNING.getColor())
                    .setDescription(context.i18n("noResults"))
                    .set("query", String.join(" ", args));
            }

            resultMessage.queue();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            AvaIre.getLogger().error("Failed to complete search query: ", e);
        }
        return false;
    }

    private boolean isAdvertisement(Elements link) {
        return link.attr("href").contains("duckduckgo.com/y.js") ||
            link.attr("href").contains("duckduckgo.com%2Fy.js") ||
            link.attr("rel").startsWith("noopener");
    }

    private String prepareLinkElement(Elements link) throws UnsupportedEncodingException {
        String[] parts = link.attr("href").split("=");

        return URLDecoder.decode(parts[parts.length - 1], "UTF-8");
    }

    private String generateUri(String[] args, boolean isNSFWEnabled) throws UnsupportedEncodingException {
        return "https://duckduckgo.com/html/?q=" + URLEncoder.encode(
            removeBangs(String.join(" ", args)), "UTF-8"
        ) + "&t=hf&ia=web&kp=" + (isNSFWEnabled ? "-2" : "1");
    }

    private String removeBangs(String text) {
        if (!Objects.equals(text.substring(0, 1), "!")) {
            return text;
        }
        return removeBangs(text.substring(1, text.length()));
    }

    private boolean isNSFWEnabled(CommandMessage message) {
        return !message.isGuildMessage() || message.getChannel().isNSFW();
    }
}
