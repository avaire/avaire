package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.RandomDogService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RandomDogCommand extends Command {

    public RandomDogCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Random Dog Command";
    }

    @Override
    public String getDescription() {
        return "I will scour the internet to find a random dog picture for you.";
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(RandomCatCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randomdog", "dog");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("https://dog.ceo/api/breeds/image/random")
            .send((Consumer<Response>) response -> {
                RandomDogService service = (RandomDogService) response.toService(RandomDogService.class);

                context.makeEmbeddedMessage().setImage(service.getMessage()).queue();
            });
        return true;
    }
}
