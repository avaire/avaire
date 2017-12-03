package com.avairebot.orion.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.fun.RandomDogCommand;
import com.avairebot.orion.contracts.ai.Intent;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.RandomDogService;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public class RequestDog extends Intent {

    public RequestDog(Orion orion) {
        super(orion);
    }

    @Override
    public String getAction() {
        return "request.dog";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        CommandContainer command = CommandHandler.getCommand(RandomDogCommand.class);

        if (command == null) {
            sendRandomDogImage(message);
            return;
        }

        command.getCommand().onCommand(message, new String[0]);
    }

    private void sendRandomDogImage(Message message) {
        RequestFactory.makeGET("https://dog.ceo/api/breeds/image/random")
            .send((Consumer<Response>) response -> {
                RandomDogService service = (RandomDogService) response.toService(RandomDogService.class);

                message.getChannel().sendMessage(
                    String.format("%s %s", message.getAuthor().getAsMention(), service.getMessage())
                ).queue();
            });
    }
}
