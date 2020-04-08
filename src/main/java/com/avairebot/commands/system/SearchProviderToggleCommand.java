/*
 * Copyright (c) 2019.
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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.audio.searcher.SearchProvider;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.utilities.ComparatorUtil;

import java.util.Arrays;
import java.util.List;

public class SearchProviderToggleCommand extends SystemCommand {

    public SearchProviderToggleCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Search Provider Toggle Command";
    }

    @Override
    public String getDescription() {
        return "Toggles music search providers on and off, disabling parts of the music handler to prevent hitting some services when looking up music.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows the status for the current music providers.",
            "`:command <provider> [status]` - Toggles the given provider on or off."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command` - Shows a status for all the music providers.",
            "`:command youtube off` - Disables the music provider."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("search-toggle", "spt");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return showStatus(context);
        }

        SearchProvider provider = SearchProvider.fromName(args[0]);
        if (provider == null) {
            return sendErrorMessage(context, "errors.invalidProperty", "provider");
        }

        switch (ComparatorUtil.getFuzzyType(args.length == 1 ? "" : args[1])) {
            case FALSE:
                provider.disable();
                break;

            case TRUE:
                provider.enable();
                break;

            case UNKNOWN:
                if (provider.isActive()) {
                    provider.disable();
                } else {
                    provider.enable();
                }
                break;
        }

        context.makeInfo("The **:provider** music provider have been **:status!**")
            .set("provider", provider)
            .set("status", provider.isActive() ? "enabled" : "disabled")
            .queue();

        return true;
    }

    private boolean showStatus(CommandMessage context) {
        StringBuilder builder = new StringBuilder();
        for (SearchProvider provider : SearchProvider.values()) {
            builder.append(provider.isActive() ? Constants.EMOTE_ONLINE : Constants.EMOTE_DND)
                .append(" ")
                .append(provider.name())
                .append("\n");
        }

        context.makeInfo(builder.toString().trim())
            .setTitle("Music Search Provider Status:")
            .queue();

        return true;
    }
}
