package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.ThreadCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

public class DuckDuckGoCommand extends ThreadCommand {

    public DuckDuckGoCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "DuckDuckGo Command";
    }

    @Override
    public String getDescription() {
        return "Search [DuckDuckGo.com](https://duckduckgo.com/) for whatever you'd like.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <query>` - Searchs DuckDuckGo for your query.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command AvaIre Bot`";
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
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `query`, you must include your search query.");
        }

        try {
            message.getChannel().sendTyping().queue();
            Document document = Jsoup.connect(
                "https://duckduckgo.com/html/?q=" + URLEncoder.encode(removeBangs(String.join(" ", args)), "UTF-8")
            ).get();

            int results = 0;
            List<String> result = new ArrayList<>();
            Elements elements = document.select("div#links div.result");
            for (Element element : elements) {
                Elements link = element.select("h2.result__title a");
                if (link.attr("href").startsWith("https://duckduckgo.com/y.js")) {
                    continue;
                }

                if (results == 0) {
                    result.add(link.attr("href") + "\n**See also**");
                    results++;
                    continue;
                }
                result.add(link.attr("href"));
                results++;

                if (results > 5) {
                    break;
                }
            }

            MessageFactory.makeEmbeddedMessage(message.getChannel(), Color.decode("#DE5833"), String.join("\n", result))
                .setTitle("Search result for: " + String.join(" ", args))
                .queue();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String removeBangs(String text) {
        if (!Objects.equals(text.substring(0, 1), "!")) {
            return text;
        }
        return removeBangs(text.substring(1, text.length()));
    }
}
