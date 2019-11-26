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

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.system.plugin.InstallPlugin;
import com.avairebot.commands.system.plugin.ListPlugins;
import com.avairebot.commands.system.plugin.ShowPlugin;
import com.avairebot.commands.system.plugin.UninstallPlugin;
import com.avairebot.contracts.commands.SystemCommand;

import java.util.Arrays;
import java.util.List;

public class PluginCommand extends SystemCommand {

    private final UninstallPlugin uninstallPlugin;
    private final InstallPlugin installPlugin;
    private final ListPlugins listPlugins;
    private final ShowPlugin showPlugin;

    public PluginCommand(AvaIre avaire) {
        super(avaire);

        uninstallPlugin = new UninstallPlugin(avaire, this);
        installPlugin = new InstallPlugin(avaire, this);
        listPlugins = new ListPlugins(avaire, this);
        showPlugin = new ShowPlugin(avaire, this);
    }

    @Override
    public String getName() {
        return "Plugin Command";
    }

    @Override
    public String getDescription() {
        return "Can be used to list installed plugins, as well as available plugins that are officially recognized by the AvaIre development team, you can also display more information about a specific plugin by name.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command show <plugin>` - Lists information about the plugin.",
            "`:command list <installed|i> [page]` - Lists installed plugins.",
            "`:command list <available|a> [page]` - Lists available plugins."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return super.getExampleUsage();
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("plugins", "plugin");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "You must parse at least one argument to the command!");
        }

        switch (args[0].toLowerCase()) {
            case "u":
            case "uninstall":
                return uninstallPlugin.onCommand(context, Arrays.copyOfRange(args, 1, args.length));

            case "i":
            case "install":
                return installPlugin.onCommand(context, Arrays.copyOfRange(args, 1, args.length));

            case "l":
            case "list":
                return listPlugins.onCommand(context, Arrays.copyOfRange(args, 1, args.length));

            case "s":
            case "show":
                return showPlugin.onCommand(context, Arrays.copyOfRange(args, 1, args.length));

            default:
                return sendErrorMessage(context, "Invalid argument given, `{0}` is not a valid action!", args[0]);
        }
    }
}
