package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.GfycatService;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GfycatCommand extends AbstractCommand {

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
        return Collections.singletonList("`>gif <query>` - Finds a random image with the given query");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("gfycat", "gif");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(message, "Missing arguments `queue`, use `!help >gif` for more information.").queue();
            return false;
        }

        try {
            RequestFactory.makeGET("https://api.gfycat.com/v1test/gfycats/search")
                    .addParameter("count", 25)
                    .addParameter("search_text", String.join(" ", args))
                    .send((Consumer<Response>) response -> {
                        GfycatService gfyCat = (GfycatService) response.toJson(GfycatService.class);

                        message.getChannel().sendMessage(gfyCat.getGfycats().get(0).get("url").toString()).queue();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
