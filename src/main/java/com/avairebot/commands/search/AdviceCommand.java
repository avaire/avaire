package com.avairebot.commands.search;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import com.avairebot.time.Carbon;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AdviceCommand extends Command
{

    public AdviceCommand(AvaIre avaire)
    {
        super(avaire);
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Advice Command";
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public String getDescription() {
        return "Returns a random piece of advice.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command` - Get a random piece of advice");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`"
        );
    }


    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    @Override
    public List<String> getTriggers() {
        return  Collections.singletonList("advice");
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        RequestFactory.makeGET("https://api.adviceslip.com/advice").send((Consumer<Response>) response -> {
            JSONObject json = new JSONObject(response.toString());
            sendAdvice(context, json);
        });
        return true;
    }

    private void sendAdvice(CommandMessage context, JSONObject json)
    {
        context.makeSuccess(json.getJSONObject("slip").getString("advice"))
            .queue();
    }
}
