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

    public ExpandUrlCommand(AvaIre avaire) {
        super(avaire);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command https://avairebot.com/support`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("expand", "resolve", "e");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "url");
        }

        try {
            List<String> redirects = fetchRedirect(args[0], new ArrayList<>());

            if (redirects.size() <= 1) {
                context.makeInfo(context.i18n("noRedirect"))
                    .set("url", args[0]).queue();
                return true;
            }


            List<String> links = new ArrayList<>();
            links.add(context.i18n("redirects", args[0], redirects.get(redirects.size() - 1)));

            if (redirects.size() > 2) {
                links.add("\n" + context.i18n("jumps"));
                for (int i = 1; i < redirects.size(); i++) {
                    links.add(redirects.get(i - 1) + " :arrow_right: " + redirects.get(i));
                }
            }

            context.makeInfo(String.join("\n", links)).queue();

            return true;
        } catch (MalformedURLException ex) {
            return sendErrorMessage(context, "errors.invalidProperty", "URL", "URL");
        } catch (UnknownHostException ex) {
            context.makeError("Unknown host for the provided `url`, does it actually go anywhere?").queue();
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
