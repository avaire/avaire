package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExpandUrlCommand extends Command {

    public ExpandUrlCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Expand Command";
    }

    @Override
    public String getDescription() {
        return "Expands the url to the full form, resolving all the redirects and showing what urls the link goes through if it redirects anywhere.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <url>` - Expands the provided url.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("expand", "resolve", "e");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `url`, you must provided a valid URL.");
        }

        try {
            List<String> redirects = fetchRedirect(args[0], new ArrayList<>());

            if (redirects.size() <= 1) {
                MessageFactory.makeInfo(message, ":url doesn't redirect anywhere.")
                    .set("url", args[0]).queue();
                return true;
            }


            List<String> links = new ArrayList<>();
            links.add(String.format("%s redirects to %s", args[0], redirects.get(redirects.size() - 1)));

            if (redirects.size() > 2) {
                links.add("\n**The link jumps through the following sites:**");
                for (int i = 1; i < redirects.size(); i++) {
                    links.add(redirects.get(i - 1) + " :arrow_right: " + redirects.get(i));
                }
            }

            MessageFactory.makeInfo(message, String.join("\n", links)).queue();

            return true;
        } catch (MalformedURLException ex) {
            return sendErrorMessage(message, "Invalid `url` provided, you must provide a valid URL.");
        } catch (UnknownHostException ex) {
            MessageFactory.makeError(message, "Unknown host for the provided `url`, does it actually go anywhere?").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<String> fetchRedirect(String url, List<String> redirects) throws IOException {
        redirects.add(url);

        HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setInstanceFollowRedirects(false);
        con.connect();

        if (con.getHeaderField("Location") == null) {
            return redirects;
        }
        return fetchRedirect(con.getHeaderField("Location"), redirects);
    }
}
