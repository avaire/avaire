package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.RandomDogService;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RandomDogCommand extends Command {

    public RandomDogCommand(Orion orion) {
        super(orion);
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
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
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
    public boolean onCommand(Message message, String[] args) {
        RequestFactory.makeGET("https://dog.ceo/api/breeds/image/random")
            .send((Consumer<Response>) response -> {
                RandomDogService service = (RandomDogService) response.toService(RandomDogService.class);

                message.getChannel().sendMessage(
                    MessageFactory.createEmbeddedBuilder()
                        .setImage(service.getMessage())
                        .build()
                ).queue();
            });
        return true;
    }
}
