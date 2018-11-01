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
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RandomUtil;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class XKCDCommand extends Command {

    public XKCDCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "XKCD Command";
    }

    @Override
    public String getDescription() {
        return "Gets the latest XKCD comic, or the comic with the given id.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Gets the latest comic",
            "`:command <id>` - Gets the comic with the given id.",
            "`:command random` - Gets a random comic."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 530` - Gets the comic with an ID of `530`.",
            "`:command random` - Gets a random comic."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("xkcd");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return requestComic(context, "https://xkcd.com/info.0.json", getLatestComicNumber());
        }

        if (NumberUtil.isNumeric(args[0])) {
            int comic = NumberUtil.getBetween(NumberUtil.parseInt(args[0]), 1, getLatestComicNumber());
            return requestComic(context, "https://xkcd.com/" + comic + "/info.0.json", comic);
        }

        if (args[0].equalsIgnoreCase("rand") || args[0].equalsIgnoreCase("random")) {
            int comic = RandomUtil.getInteger(getLatestComicNumber()) + 1;
            return requestComic(context, "https://xkcd.com/" + comic + "/info.0.json", comic);
        }

        return sendErrorMessage(context, context.i18n("invalidArgument", getLatestComicNumber()));
    }

    private boolean requestComic(CommandMessage context, String url, int comic) {
        RequestFactory.makeGET(url).send((Consumer<Response>) response -> {
            JSONObject json = new JSONObject(response.toString());

            Carbon date = Carbon.today()
                .setYear(json.getInt("year"))
                .setMonth(json.getInt("month"))
                .setDay(json.getInt("day"));

            sendComic(context, json, date, comic);
        });
        return true;
    }

    private void sendComic(CommandMessage context, JSONObject json, Carbon date, int comic) {
        context.makeEmbeddedMessage(
            Color.decode("#96A8C8"), String.format("**%s**\n%s", json.getString("safe_title"), json.getString("alt"))
        )
            .setImage(json.getString("img"))
            .setTimestamp(date.getTime().toInstant())
            .setFooter(context.i18n("number", comic))
            .queue();
    }

    private int getLatestComicNumber() {
        return ((Double) avaire.getCache().getAdapter(CacheType.FILE).remember("xkcd.latest", 720, () -> {
            try {
                Connection.Response response = Jsoup.connect("https://xkcd.com/info.0.json")
                    .ignoreContentType(true).execute();

                return new JSONObject(response.body()).get("num");
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        })).intValue();
    }
}
