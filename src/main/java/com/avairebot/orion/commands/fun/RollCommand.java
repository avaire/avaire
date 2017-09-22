package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RollCommand extends AbstractCommand {

    public RollCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Roll Command";
    }

    @Override
    public String getDescription() {
        return "Roll a random number between 1 and 100, or within the given parameters.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
                "`:command`",
                "`:command [max]`",
                "`:command [min] [max]`"
        );
    }

    @Override
    public String getExampleUsage() {
        return "`:command 3 6`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("roll");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        double min = 1;
        double max = 100;

        try {
            if (args.length == 1) {
                max = parseArgument(message, args[0], "Invalid `max` value given, `max` must be a number!");
            } else if (args.length > 1) {
                min = parseArgument(message, args[0], "Invalid `min` value given, `min` must be a number!");
                max = parseArgument(message, args[1], "Invalid `max` value given, `max` must be a number!");
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        min = Math.ceil(min);
        max = Math.floor(max);

        double random = Math.floor(Math.random() * (max - min + 1)) + min;

        MessageFactory.makeInfo(message, "<@%s> rolled **%s** out of %s - %s",
                message.getAuthor().getId(),
                (int) random, (int) min, (int) max
        ).queue();

        return true;
    }

    private double parseArgument(Message message, String argument, String error) throws NumberFormatException {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException ex) {
            sendErrorMessage(message, error);
            throw ex;
        }
    }
}
