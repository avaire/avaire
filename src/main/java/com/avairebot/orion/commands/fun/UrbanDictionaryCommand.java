package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.UrbanDictionaryService;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class UrbanDictionaryCommand extends Command {

    public UrbanDictionaryCommand(Orion orion) {
        super(orion);
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
    public String getExampleUsage() {
        return "`:command potato`";
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
    public boolean onCommand(Message message, String[] args) {
        RequestFactory.makeGET("https://api.urbandictionary.com/v0/define")
            .addParameter("term", String.join(" ", args))
            .send((Consumer<Response>) response -> {
                UrbanDictionaryService service = (UrbanDictionaryService) response.toService(UrbanDictionaryService.class);

                if (!service.hasData()) {
                    MessageFactory.makeWarning(message, ":user I found nothing for `:query`")
                        .set("query", String.join(" ", args))
                        .queue();
                    return;
                }

                UrbanDictionaryService.UrbanDictionary definition = service.getList().get(0);

                double percentage = (((double) definition.getThumbsUp() / definition.getThumbsDown()) * 100) - 100;

                EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                    .setColor(Color.decode("#1D2439"))
                    .setTitle(definition.getWord(), definition.getPermalink())
                    .setDescription(definition.getDefinition())
                    .addField("Example", definition.getExample(), false)
                    .setFooter(String.format("%s%s percentage of people like this. %s\uD83D\uDC4D %s\uD83D\uDC4E",
                        new DecimalFormat("#.##").format(percentage), "%",
                        definition.getThumbsUp(),
                        definition.getThumbsDown()
                    ), null);

                message.getChannel().sendMessage(embed.build()).queue();
            });
        return true;
    }
}
