package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CSSHumor extends Command
{
    private static String url = "https://csshumor.com/";

    public CSSHumor(AvaIre avaire)
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
        return "CSSHumor";
    }

    @Override
    public String getDescription() {
        return "Returns a random css joke.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - retrieves a displays a css joke");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
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
        return Collections.singletonList("csshumor");
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
        RequestFactory.makeGET(url)
            .send((Consumer<Response>) response ->
            {
                String css = getCss(response.toString());

                if(!css.isEmpty())
                {
                    context.makeSuccess(css).queue();
                }
                else
                {
                    context.makeError(context.i18n("notFound"));
                }
            });
        return true;
    }

    private String getCss(String html)
    {
        try
        {
            StringBuilder builder = new StringBuilder();
            Elements elements = Jsoup.parse(html).getElementsByClass("crayon-line");

            for(Element elementLine : elements )
            {
                builder.append(elementLine.text());
            }
            return builder.toString();
        }
        catch(Error error)
        {
            return "";
        }
    }


}
