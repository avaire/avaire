package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandMessage;
import com.avairebot.orion.contracts.middleware.Middleware;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessCommand extends Middleware {

    private final static String COMMAND_OUTPUT = "Executing Command \"%command%\" in \"%category%\" category:"
            + "\n\t\tUser:\t %author%"
            + "\n\t\tServer:\t %server%"
            + "\n\t\tChannel: %channel%"
            + "\n\t\tMessage: %message%";

    private final Pattern argumentsRegEX;

    public ProcessCommand(Orion orion) {
        super(orion);

        this.argumentsRegEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*", Pattern.MULTILINE);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        String[] arguments = generateCommandArguments(message);

        orion.logger.info(COMMAND_OUTPUT
                .replace("%command%", stack.getCommand().getName())
                .replace("%category%", stack.getCommandContainer().getCategory().getName())
                .replace("%author%", generateUsername(message))
                .replace("%server%", generateServer(message))
                .replace("%channel%", generateChannel(message))
                .replace("%message%", message.getRawContent())
        );

        return stack.getCommand().onCommand(
                new CommandMessage(message, stack.isMentionableCommand()),
                Arrays.copyOfRange(arguments, stack.isMentionableCommand() ? 2 : 1, arguments.length)
        );
    }

    private String[] generateCommandArguments(Message message) {
        List<String> arguments = new ArrayList<>();

        Matcher matcher = argumentsRegEX.matcher(message.getRawContent());
        while (matcher.find()) {
            arguments.add(matcher.group(0)
                    .replaceAll("\"", "")
                    .trim());
        }

        return arguments.toArray(new String[0]);
    }

    private String generateUsername(Message message) {
        return String.format("%s#%s [%s]",
                message.getAuthor().getName(),
                message.getAuthor().getDiscriminator(),
                message.getAuthor().getId()
        );
    }

    private String generateServer(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
                message.getGuild().getName(),
                message.getGuild().getId()
        );
    }

    private CharSequence generateChannel(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
                message.getChannel().getName(),
                message.getChannel().getId()
        );
    }
}
