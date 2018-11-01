/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.factories.MessageFactory;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class IPInfoCommand extends Command {

    private final Pattern urlRegEX = Pattern.compile(
        "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$"
    );

    public IPInfoCommand(AvaIre avaire) {
        super(avaire);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 8.8.4.4`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ipinfo");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument `ip`, you must include a valid IP address.");
        }

        if (!urlRegEX.matcher(args[0]).find()) {
            return sendErrorMessage(context, "Invalid IP address given, you must parse a valid IP address.");
        }

        RequestFactory.makeGET("http://ipinfo.io/" + args[0] + "/json").send((Consumer<Response>) response -> {
            JSONObject json = new JSONObject(response.toString());

            MessageFactory.makeEmbeddedMessage(context.getChannel(), Color.decode("#005A8C"),
                new MessageEmbed.Field(context.i18n("hostname"), json.has("hostname") ? json.getString("hostname") : "Unknown", true),
                new MessageEmbed.Field(context.i18n("organisation"), json.has("org") ? json.getString("org") : "Unknown", true),
                new MessageEmbed.Field(context.i18n("country"), generateLocation(json), false)
            ).setTitle(args[0]).setFooter(generateFooter(context.getMessage()), null).queue();
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
