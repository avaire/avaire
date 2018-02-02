package com.avairebot.commands.search;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.GfycatService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GfycatCommand extends Command {

    public GfycatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Gfycat Command";
    }

    @Override
    public String getDescription() {
        return "Returns a random gif for you from gfycat.com with the given query.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <query>` - Finds a random image with the given query");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command cats`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("gfycat", "gif");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing arguments `queue`");
        }

        RequestFactory.makeGET("https://api.gfycat.com/v1test/gfycats/search")
            .addParameter("count", 25)
            .addParameter("search_text", String.join(" ", args))
            .send((Consumer<Response>) response -> {
                GfycatService gfyCat = (GfycatService) response.toService(GfycatService.class);

                Map<String, Object> gfycatsItem = gfyCat.getRandomGfycatsItem();
                if (gfycatsItem == null) {
                    context.makeError("I couldn't find any gif matching your query: `:query`")
                        .set("query", String.join(" ", args))
                        .queue();

                    return;
                }

                context.makeEmbeddedMessage()
                    .setImage(gfycatsItem.get("gifUrl").toString())
                    .queue();
            });
        return true;
    }
}
