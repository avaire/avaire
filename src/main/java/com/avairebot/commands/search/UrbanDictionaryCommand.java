package com.avairebot.commands.search;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.UrbanDictionaryService;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class UrbanDictionaryCommand extends Command {

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
        RequestFactory.makeGET("https://api.urbandictionary.com/v0/define")
            .addParameter("term", String.join(" ", args))
            .send((Consumer<Response>) response -> {
                UrbanDictionaryService service = (UrbanDictionaryService) response.toService(UrbanDictionaryService.class);

                if (!service.hasData()) {
                    context.makeWarning(":user I found nothing for `:query`")
                        .set("query", String.join(" ", args))
                        .queue();
                    return;
                }

                UrbanDictionaryService.UrbanDictionary definition = service.getList().get(0);

                double thumbsUp = definition.getThumbsUp();
                double thumbsDown = definition.getThumbsDown();
                double percentage = (thumbsUp / (thumbsUp + thumbsDown)) * 100;

                context.makeEmbeddedMessage(Color.decode("#1D2439"), definition.getDefinition())
                    .setTitle(definition.getWord().trim().length() == 0 ? "Untitled" : definition.getWord(), definition.getPermalink())
                    .addField("Example", definition.getExample(), false)
                    .setFooter(String.format("%s%s percentage of people like this. %s\uD83D\uDC4D %s\uD83D\uDC4E",
                        new DecimalFormat("#.##").format(percentage), "%",
                        definition.getThumbsUp(),
                        definition.getThumbsDown()
                    )).queue();
            });
        return true;
    }
}
