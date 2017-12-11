package com.avairebot.orion.commands.search;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.GfycatService;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GfycatCommand extends Command {

    public GfycatCommand(Orion orion) {
        super(orion);
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
    public String getExampleUsage() {
        return "`:command cats`";
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
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing arguments `queue`");
        }

        RequestFactory.makeGET("https://api.gfycat.com/v1test/gfycats/search")
            .addParameter("count", 25)
            .addParameter("search_text", String.join(" ", args))
            .send((Consumer<Response>) response -> {
                GfycatService gfyCat = (GfycatService) response.toService(GfycatService.class);

                MessageFactory.makeEmbeddedMessage(message.getChannel(), Color.decode("#"))
                    .setImage(gfyCat.getRandomGfycatsItem().get("gifUrl").toString())
                    .queue();
            });
        return true;
    }
}
