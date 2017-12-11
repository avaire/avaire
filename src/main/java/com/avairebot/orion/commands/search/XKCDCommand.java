package com.avairebot.orion.commands.search;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.contracts.commands.ThreadCommand;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.time.Carbon;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class XKCDCommand extends ThreadCommand {

    public XKCDCommand(Orion orion) {
        super(orion);
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
            "`:command <id>` - Gets the comic with the given id."
        );
    }

    @Override
    public String getExampleUsage() {
        return null;
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
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return requestComic(message, "https://xkcd.com/info.0.json", getLatestComicNumber());
        }

        int comic = NumberUtil.getBetween(NumberUtil.parseInt(args[0]), 1, getLatestComicNumber());
        return requestComic(message, "https://xkcd.com/" + comic + "/info.0.json", comic);
    }

    private boolean requestComic(Message message, String url, int comic) {
        RequestFactory.makeGET(url).send((Consumer<Response>) response -> {
            JSONObject json = new JSONObject(response.toString());

            Carbon date = Carbon.today()
                .setYear(json.getInt("year"))
                .setMonth(json.getInt("month"))
                .setDay(json.getInt("day"));

            sendComic(message, json, date, comic);
        });
        return true;
    }

    private void sendComic(Message message, JSONObject json, Carbon date, int comic) {
        MessageFactory.makeEmbeddedMessage(message, Color.decode("#96A8C8"),
            String.format("**%s**\n%s", json.getString("safe_title"), json.getString("alt"))
        )
            .setImage(json.getString("img"))
            .setTimestamp(date.getTime().toInstant())
            .setFooter("XKCD number #" + comic)
            .queue();
    }

    private int getLatestComicNumber() {
        return ((Double) orion.getCache().getAdapter(CacheType.FILE).remember("xkcd.latest", 720, () -> {
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
