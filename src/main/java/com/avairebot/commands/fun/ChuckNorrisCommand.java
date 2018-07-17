package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.ChuckNorrisService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

public class ChuckNorrisCommand extends Command {

    public ChuckNorrisCommand(AvaIre avaire) {
        super(avaire);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("http://api.icndb.com/jokes/random")
            .addParameter("escape", "javascript")
            .send((Consumer<Response>) response -> {
                ChuckNorrisService service = (ChuckNorrisService) response.toService(ChuckNorrisService.class);

                context.makeSuccess(prepareJoke(context, args, service.getValue().getJoke())).queue();
            });
        return true;
    }

    private String prepareJoke(CommandMessage message, String[] args, String joke) {
        if (!message.getMentionedUsers().isEmpty()) {
            return joke.replaceAll("Chuck Norris", Matcher.quoteReplacement(
                message.getGuild().getMember(
                    message.getMentionedUsers().get(0)
                ).getEffectiveName()
            ));
        }

        if (args.length > 0) {
            return joke.replaceAll("Chuck Norris", Matcher.quoteReplacement(String.join(" ", args)));
        }

        return joke;
    }
}
