package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.JSONObject;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class IPInfoCommand extends Command {

    private final Pattern urlRegEX = Pattern.compile(
        "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$"
    );

    public IPInfoCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "IP Info Command";
    }

    @Override
    public String getDescription() {
        return "Gives information about the given IP address.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <ip>` - Displays information about the given IP address.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command 8.8.4.4`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ipinfo");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `ip`, you must include a valid IP address.");
        }

        if (!urlRegEX.matcher(args[0]).find()) {
            return sendErrorMessage(message, "Invalid IP address given, you must parse a valid IP address.");
        }

        RequestFactory.makeGET("http://ipinfo.io/" + args[0] + "/json").send((Consumer<Response>) response -> {
            JSONObject json = new JSONObject(response.toString());

            MessageFactory.makeEmbeddedMessage(message.getChannel(), Color.decode("#005A8C"),
                new MessageEmbed.Field("Hostname", json.has("hostname") ? json.getString("hostname") : "Unknown", true),
                new MessageEmbed.Field("Organisation", json.has("org") ? json.getString("org") : "Unknown", true),
                new MessageEmbed.Field("Country", generateLocation(json), false)
            ).setTitle(args[0]).setFooter(generateFooter(message), null).queue();
        });

        return true;
    }

    private String generateLocation(JSONObject json) {
        String flag = "";
        if (json.has("country")) {
            flag = String.format(":flag_%s: ", json.getString("country").toLowerCase());
        }

        return String.format("%s%s, %s, %s",
            flag,
            json.has("city") && json.getString("city").length() > 1 ? json.getString("city") : "Unknown",
            json.has("region") && json.getString("region").length() > 1 ? json.getString("region") : "Unknown",
            json.has("loc") && json.getString("loc").length() > 1 ? json.getString("loc") : "Unknown"
        );
    }

    private String generateFooter(Message message) {
        return String.format("Requested by %s#%s (%s)",
            message.getAuthor().getName(),
            message.getAuthor().getDiscriminator(),
            message.getAuthor().getId()
        );
    }
}
