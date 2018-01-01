package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.ChuckNorrisService;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ChuckNorrisCommand extends Command {

    public ChuckNorrisCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Chuck Norris Command";
    }

    @Override
    public String getDescription() {
        return "I will get a random 100% true, real facts about Chuck Norris for you using the \"Internet Chuck Norris Database\".";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [name]` - Gets a random fact for you, if a name is given \"Chuck Norris\" will be replaced with the given name.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Senither`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("chucknorris", "chuck", "norris");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        RequestFactory.makeGET("http://api.icndb.com/jokes/random")
            .addParameter("escape", "javascript")
            .send((Consumer<Response>) response -> {
                ChuckNorrisService service = (ChuckNorrisService) response.toService(ChuckNorrisService.class);

                MessageFactory.makeSuccess(message, prepareJoke(message, args, service.getValue().getJoke())).queue();
            });
        return true;
    }

    private String prepareJoke(Message message, String[] args, String joke) {
        if (!message.getMentionedUsers().isEmpty()) {
            return joke.replaceAll("Chuck Norris", message.getGuild().getMember(
                message.getMentionedUsers().get(0)
            ).getEffectiveName());
        }

        if (args.length > 0) {
            return joke.replaceAll("Chuck Norris", String.join(" ", args));
        }

        return joke;
    }
}
