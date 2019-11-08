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
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.system.PluginCommand;
import com.avairebot.contracts.commands.plugin.PluginSubCommand;
import com.avairebot.contracts.plugin.Plugin;
import com.avairebot.contracts.plugin.PluginAsset;
import com.avairebot.contracts.plugin.PluginRelease;
import com.avairebot.contracts.plugin.PluginSourceManager;
import com.avairebot.exceptions.InvalidPluginException;
import com.avairebot.exceptions.InvalidPluginsPathException;
import com.avairebot.plugin.PluginLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class InstallPlugin extends PluginSubCommand {

    /**
     * Creates a new plugin sub command instance.
     *
     * @param avaire  The main avaire application instance.
     * @param command The parent plugin command instance.
     */
    public InstallPlugin(AvaIre avaire, PluginCommand command) {
        super(avaire, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return command.sendErrorMessage(context, "You must include the name of the plugin you'd like to install!");
        }

        Plugin plugin = getPluginByName(args[0]);
        if (plugin == null) {
            return command.sendErrorMessage(context, "Couldn't find any plugin called `{0}`, are you sure it exists?", args[0]);
        }

        if (plugin.isInstalled()) {
            return command.sendErrorMessage(context, "The `{0}` plugin is already installed!", plugin.getName());
        }

        if (args.length == 1) {
            return command.sendErrorMessage(context, "You must include the version of the `{0}` plugin you wish to install, or use `latest` to install the latest version of the plugin.", plugin.getName());
        }

        PluginRelease version = getVersionOfPlugin(plugin, args[1]);
        if (version == null) {
            return command.sendErrorMessage(context, "Invalid version selected, `{0}` is not valid version for the `{1}` plugin.", args[1], plugin.getName());
        }

        PluginAsset downloadAsset = null;
        for (PluginAsset pluginAsset : version.getAssets()) {
            if (pluginAsset.getName().endsWith(".jar")) {
                downloadAsset = pluginAsset;
                break;
            }
        }

        if (downloadAsset == null) {
            return command.sendErrorMessage(context, "The `{0}` version for the `{1}` plugin has no downloadable JAR file assets, unable to download and install the plugin version, please report this to the original plugin author so they can fix it.");
        }

        File pluginFile = new File(avaire.getPluginManager().getPluginsFolder(), downloadAsset.getName());
        if (!downloadPluginAsset(downloadAsset, pluginFile)) {
            return command.sendErrorMessage(context, "Failed to download the plugin, try check the console for errors.");
        }

        try {
            PluginLoader pluginLoader = avaire.getPluginManager().loadPlugin(pluginFile);

            pluginLoader.invokePlugin(avaire);

            createPluginIndex(plugin, version, downloadAsset);

            context.makeSuccess("The **:name** plugin have successfully been installed with the **:version** version!\nYou can now use the features from the plugin.")
                .set("name", plugin.getName())
                .set("version", version.getTag())
                .queue();
        } catch (InvalidPluginsPathException | InvalidPluginException e) {
            pluginFile.delete();

            return command.sendErrorMessage(context, "Failed to load the plugin, the plugin failed with the follow error: {0}", e.getMessage());
        }

        return true;
    }

    private PluginRelease getVersionOfPlugin(Plugin plugin, String version) {
        PluginSourceManager sourceManager = plugin.getRepository().getSource().getSourceManager();

        if (version.equalsIgnoreCase("latest")) {
            return sourceManager.getPluginReleases(plugin.getRepository()).get(0);
        }

        for (PluginRelease release : sourceManager.getPluginReleases(plugin.getRepository())) {
            if (release.getTag().equalsIgnoreCase(version)) {
                return release;
            }
        }

        return null;
    }

    private boolean downloadPluginAsset(PluginAsset pluginAsset, File output) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(pluginAsset.getDownloadableUrl()).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(output)) {

            byte dataBuffer[] = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
