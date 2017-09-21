package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.RandomDogService;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RandomDogCommand extends AbstractCommand {

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
        return Collections.singletonList("`>randomdog`");
    }

    @Override
    public String getExampleUsage() {
        return "`>randomdog`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randomdog", "dog");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        try {
            RequestFactory.makeGET("https://dog.ceo/api/breeds/image/random")
                    .send((Consumer<Response>) response -> {
                        RandomDogService service = (RandomDogService) response.toJson(RandomDogService.class);

                        message.getChannel().sendMessage(service.getMessage()).queue();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
