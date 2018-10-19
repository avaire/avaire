package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.factories.RequestFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UndertaleTextBoxCommand extends Command
{

    private String templateUrl = "www.demirramon.com/utgen.png?message=%s&character=%s";

    public UndertaleTextBoxCommand(AvaIre avaire) {
        super(avaire);
    }


    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName()
    {
        return "Undertale TextBox Command";
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command toriel Greetings,my child`");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public String getDescription()
    {
        return ":command [message] [character] : Returns an undertale textbox image containing the specified text.";
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
        return Arrays.asList("utbox", "textbox");
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The JDA message object from the message received event.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        if (args.length == 0)
        {
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }
        else
        {
            try {
                context.makeEmbeddedMessage()
                    .setImage(String.format(
                        templateUrl,
                        URLEncoder.encode(args[0],"UTF-8").replace("+","%20"),
                        args[1].toLowerCase()
                    )).queue();
            } catch (UnsupportedEncodingException e)
            {
                context.makeError(e.getMessage());
                return false;
            }

            return true;
        }
    }


}
