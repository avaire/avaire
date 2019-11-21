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

package com.avairebot.commands.system.plugin;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.system.PluginCommand;
import com.avairebot.contracts.commands.plugin.PluginSubCommand;
import com.avairebot.contracts.plugin.Plugin;
import com.avairebot.contracts.plugin.PluginRelease;
import com.avairebot.contracts.plugin.PluginSourceManager;

import java.util.ArrayList;
import java.util.List;

public class ShowPlugin extends PluginSubCommand {

    /**
     * Creates a new plugin sub command instance.
     *
     * @param avaire  The main avaire application instance.
     * @param command The parent plugin command instance.
     */
    public ShowPlugin(AvaIre avaire, PluginCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return command.sendErrorMessage(context, "You must include the name of the plugin you'd like to see information about!");
        }

        Plugin plugin = avaire.getPluginManager().getPluginByName(args[0]);
        if (plugin == null) {
            return command.sendErrorMessage(context, "Couldn't find any plugin called `{0}`, are you sure it exists?", args[0]);
        }

        PlaceholderMessage message = context.makeInfo(plugin.getDescription())
            .setTitle(plugin.getName())
            .addField("Source URL:", String.format("[%s](%s)",
                plugin.getRepository().getRepository(),
                plugin.getRepository().getSource().getSourceUrl(plugin.getRepository().getRepository())
            ), false)
            .addField("Created by", String.join("\n", plugin.getAuthors()), true)
            .addField("Installed", plugin.isInstalled() ? "Yes" : "No", true);

        List<String> versions = new ArrayList<>();
        PluginSourceManager sourceManager = plugin.getRepository().getSource().getSourceManager();
        for (PluginRelease release : sourceManager.getPluginReleases(plugin.getRepository())) {
            versions.add("`" + release.getTag() + "`");
            if (versions.size() >= 30) {
                break;
            }
        }

        if (versions.isEmpty()) {
            message.addField("Available Versions:", "*There are currently no publicly available versions*", false);
        } else {
            message.addField("Available Versions:", String.join(", ", versions), false);
        }

        message.queue();

        return true;
    }
}
