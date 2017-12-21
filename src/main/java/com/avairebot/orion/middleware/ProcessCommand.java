package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.AliasCommandContainer;
import com.avairebot.orion.commands.CommandMessage;
import com.avairebot.orion.contracts.commands.ThreadCommand;
import com.avairebot.orion.contracts.middleware.Middleware;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.metrics.Metrics;
import com.avairebot.orion.utilities.ArrayUtil;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import io.prometheus.client.Histogram;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ProcessCommand extends Middleware {

    private final static String COMMAND_OUTPUT = "Executing Command \"%command%\" in \"%category%\" category:"
        + "\n\t\tUser:\t %author%"
        + "\n\t\tServer:\t %server%"
        + "\n\t\tChannel: %channel%"
        + "\n\t\tMessage: %message%";

    public ProcessCommand(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        String[] arguments = ArrayUtil.toArguments(message.getRawContent());

        Orion.getLogger().info(COMMAND_OUTPUT
            .replace("%command%", stack.getCommand().getName())
            .replace("%category%", stack.getCommandContainer().getCategory().getName())
            .replace("%author%", generateUsername(message))
            .replace("%server%", generateServer(message))
            .replace("%channel%", generateChannel(message))
            .replace("%message%", message.getRawContent())
        );

        Histogram.Timer timer = null;

        try {
            String[] commandArguments = Arrays.copyOfRange(arguments, stack.isMentionableCommand() ? 2 : 1, arguments.length);
            if (stack.getCommandContainer() instanceof AliasCommandContainer) {
                AliasCommandContainer container = (AliasCommandContainer) stack.getCommandContainer();

                return runCommand(stack,
                    new CommandMessage(message, stack.isMentionableCommand(), container.getAliasArguments()),
                    combineArguments(container.getAliasArguments(), commandArguments)
                );
            }

            Metrics.commandsExecuted.labels(stack.getCommand().getClass().getSimpleName()).inc();

            timer = Metrics.executionTime.labels(stack.getCommand().getClass().getSimpleName()).startTimer();

            return runCommand(stack, new CommandMessage(message, stack.isMentionableCommand()), commandArguments);
        } catch (Exception ex) {
            Metrics.commandExceptions.labels(ex.getClass().getSimpleName()).inc();

            if (ex instanceof FriendlyException) {
                MessageFactory.makeError(message, "Error: " + ex.getMessage())
                    .queue(newMessage -> newMessage.delete().queueAfter(30, TimeUnit.SECONDS));
            }

            ex.printStackTrace();
            return false;
        } finally {
            if (timer != null) {
                timer.observeDuration();
            }
        }
    }

    private boolean runCommand(MiddlewareStack stack, CommandMessage message, String[] args) {
        if (stack.getCommand() instanceof ThreadCommand) {
            ((ThreadCommand) stack.getCommand()).runThreadCommand(message, args);
            return true;
        }

        return stack.getCommand().onCommand(message, args);
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

    private String[] combineArguments(String[] aliasArguments, String[] userArguments) {
        int length = aliasArguments.length + userArguments.length;

        String[] result = new String[length];

        System.arraycopy(aliasArguments, 0, result, 0, aliasArguments.length);
        System.arraycopy(userArguments, 0, result, aliasArguments.length, userArguments.length);

        return result;
    }
}
