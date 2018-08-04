package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Request;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.RandomCatService;
import com.avairebot.utilities.RestActionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RandomCatCommand extends Command {

    public RandomCatCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Random Cat Command";
    }

    @Override
    public String getDescription() {
        return "I will scour the internet to find a random cat picture for you.";
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(RandomDogCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randomcat", "cat");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Request request = RequestFactory.makeGET("https://meow.senither.com/v1/random");

        String token = avaire.getConfig().getString("apiKeys.meowApi", null);
        if (token != null && token.length() > 0) {
            request.addParameter("token", token);
        }

        request.send((Consumer<Response>) response -> {
            int statusCode = response.getResponse().code();

            if (statusCode == 429) {
                context.makeWarning("Too many attempts was made to the cat API, try again in a minute, or ask a bot administrator to get an API key at [meow.senither.com](https://meow.senither.com/) to allow for more requests.")
                    .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                return;
            }

            if (statusCode == 404) {
                context.makeWarning("I couldn't find any cat picture D: Try again, maybe they will show up now?")
                    .queue(message -> message.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                return;
            }

            if (statusCode == 200) {
                RandomCatService service = (RandomCatService) response.toService(RandomCatService.class);
                context.makeEmbeddedMessage().setImage(service.getData().getUrl()).queue();

                return;
            }

            context.makeError("Something just went horribly wrong, please tell a bot administrator so they can look into this.").queue();
        });
        return true;
    }
}
