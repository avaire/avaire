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
import com.avairebot.cache.CacheType;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.contracts.plugin.Translator;
import com.avairebot.plugin.PluginHolder;
import com.avairebot.plugin.PluginLoader;
import com.avairebot.plugin.translators.PluginHolderTranslator;
import com.avairebot.plugin.translators.PluginLoaderTranslator;
import com.avairebot.utilities.NumberUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginCommand extends SystemCommand {

    public PluginCommand(AvaIre avaire) {
        super(avaire);
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

        if (args[0].equalsIgnoreCase("list")) {
            return listPlugins(context, Arrays.copyOfRange(args, 1, args.length));
        }

        if (args[0].equalsIgnoreCase("show")) {
            return showPlugin(context, Arrays.copyOfRange(args, 1, args.length));
        }

        return sendErrorMessage(context, "Invalid argument given, `{0}` is not a valid action!", args[0]);
    }

    private boolean listPlugins(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, String.join("\n", Arrays.asList(
                "You must choose the type of listing you want to see!",
                "",
                "Installed plugins: `installed` `i`",
                "Available plugins: `available`, `a`"
            )));
        }

        if (args[0].equalsIgnoreCase("installed") || args[0].equalsIgnoreCase("i")) {
            if (avaire.getPluginManager().getPlugins().isEmpty()) {
                return sendErrorMessage(context, "There are currently no installed plugins!");
            }

            List<String> messages = new ArrayList<>();
            SimplePaginator<PluginLoader> paginator = new SimplePaginator<>(
                avaire.getPluginManager().getPlugins().iterator(), 5, 1
            );

            if (args.length > 1) {
                paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
            }

            paginator.forEach((index, key, loader) -> {
                messages.add("**" + loader.getName() + "**");
            });

            messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage()) + " list installed"));

            context.makeInfo(String.join("\n", messages))
                .setTitle("Installed Plugins")
                .queue();

            return true;
        }

        if (args[0].equalsIgnoreCase("available") || args[0].equalsIgnoreCase("a")) {
            List<PluginHolder> pluginHolders = fetchPluginsFromGithub();
            if (pluginHolders == null) {
                return sendErrorMessage(context, "Failed to load plugins for the plugins list, try again later.");
            }

            List<String> messages = new ArrayList<>();
            SimplePaginator<PluginHolder> paginator = new SimplePaginator<>(pluginHolders, 5, 1);
            if (args.length > 1) {
                paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
            }

            paginator.forEach((index, key, loader) -> {
                messages.add("**" + loader.getName() + "**");
            });

            messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage()) + " list available"));

            context.makeInfo(String.join("\n", messages))
                .setTitle("Available Plugins")
                .queue();

            return false;
        }

        return sendErrorMessage(context, "Invalid listing type given!");
    }

    private boolean showPlugin(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "You must include the name of the plugin you'd like to see information about!");
        }

        Translator plugin = getPluginByName(args[0]);
        if (plugin == null) {
            return sendErrorMessage(context, "Couldn't find any plugin called `{0}`, are you sure it exists?", args[0]);
        }

        context.makeInfo(plugin.getDescription())
            .setTitle(plugin.getName())
            .addField("Source URL:", String.format("[%s](%s)",
                plugin.getRepository().getRepository(),
                plugin.getRepository().getSource().getSourceUrl(plugin.getRepository().getRepository())
            ), false)
            .addField("Created by", String.join("\n", plugin.getAuthors()), true)
            .addField("Installed", plugin.isInstalled() ? "Yes" : "No", true)
            .queue();

        return true;
    }

    private Translator getPluginByName(String name) {
        List<PluginHolder> pluginHolders = fetchPluginsFromGithub();
        if (pluginHolders == null) {
            return null;
        }

        for (PluginLoader pluginLoader : avaire.getPluginManager().getPlugins()) {
            if (pluginLoader.getName().equalsIgnoreCase(name)) {
                return new PluginLoaderTranslator(pluginLoader, fetchPluginsFromGithub());
            }
        }

        for (PluginHolder holder : pluginHolders) {
            if (holder.getName().equalsIgnoreCase(name)) {
                return new PluginHolderTranslator(holder);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private List<PluginHolder> fetchPluginsFromGithub() {
        Object plugins = avaire.getCache().getAdapter(CacheType.MEMORY).remember("plugins", 10800, () -> {
            try {
                Connection.Response execute = Jsoup.connect("https://raw.githubusercontent.com/avaire/plugins/master/plugins.json")
                    .ignoreContentType(true).execute();

                JSONObject obj = new JSONObject(execute.body());
                JSONArray data = obj.getJSONArray("data");

                List<PluginHolder> pluginList = new ArrayList<>();
                for (Object aData : data) {
                    pluginList.add(new PluginHolder((JSONObject) aData));
                }
                return pluginList;
            } catch (IOException e) {
                AvaIre.getLogger().error("Failed to fetch plugins from github: " + e.getMessage(), e);
                return null;
            }
        });

        if (plugins instanceof List) {
            return (List<PluginHolder>) plugins;
        }
        return null;
    }
}
