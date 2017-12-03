package com.avairebot.orion.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.fun.RandomCatCommand;
import com.avairebot.orion.contracts.ai.Intent;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.RandomCatService;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public class RequestCat extends Intent {

    public RequestCat(Orion orion) {
        super(orion);
    }

    @Override
    public String getAction() {
        return "request.cat";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        CommandContainer command = CommandHandler.getCommand(RandomCatCommand.class);

        if (command == null) {
            sendRandomCatImage(message);
            return;
        }

        command.getCommand().onCommand(message, new String[0]);
    }

    private void sendRandomCatImage(Message message) {
        RequestFactory.makeGET("http://random.cat/meow")
            .send((Consumer<Response>) response -> {
                RandomCatService service = (RandomCatService) response.toService(RandomCatService.class);

                message.getChannel().sendMessage(
                    String.format("%s %s", message.getAuthor().getAsMention(), service.getFile())
                ).queue();
            });
    }
}
