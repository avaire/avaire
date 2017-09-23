package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ProcessCommand extends AbstractMiddleware {

    private final Pattern argumentsRegEX;

    public ProcessCommand(Orion orion) {
        super(orion);

        this.argumentsRegEX = Pattern.compile("[\\s\"]+|\"([^\"]*)\"", Pattern.MULTILINE);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        String[] arguments = argumentsRegEX.split(message.getContent());

        User author = message.getAuthor();
        orion.logger.info(String.format("Executing Command <%s> from %s#%s",
                message.getContent(),
                author.getName(),
                author.getDiscriminator()
        ));

        return stack.getCommand().onCommand(message, Arrays.copyOfRange(arguments, 1, arguments.length));
    }
}
