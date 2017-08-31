package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ProcessCommand extends AbstractMiddleware {

    private final Pattern argumentsRegEX;

    public ProcessCommand(Orion orion) {
        super(orion);

        this.argumentsRegEX = Pattern.compile("[\\s\"]+|\"([^\"]*)\"", Pattern.MULTILINE);
    }

    @Override
    public void handle(MessageReceivedEvent event, MiddlewareStack stack, String... args) {
        String[] arguments = argumentsRegEX.split(event.getMessage().getContent());

        User author = event.getMessage().getAuthor();
        orion.logger.info("Executing Command <%s> from %s#%s",
                event.getMessage().getContent(),
                author.getName(),
                author.getDiscriminator()
        );

        stack.getCommand().onCommand(event, Arrays.copyOfRange(arguments, 1, arguments.length));
    }
}
