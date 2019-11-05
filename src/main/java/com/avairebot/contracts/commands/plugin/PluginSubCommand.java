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

package com.avairebot.contracts.commands.plugin;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.system.PluginCommand;
import com.avairebot.contracts.plugin.Translator;
import com.avairebot.plugin.PluginHolder;
import com.avairebot.plugin.PluginLoader;
import com.avairebot.plugin.translators.PluginHolderTranslator;
import com.avairebot.plugin.translators.PluginLoaderTranslator;

import java.util.List;

public abstract class PluginSubCommand {

    /**
     * The main {@link AvaIre avaire} application instance.
     */
    protected final AvaIre avaire;

    /**
     * The parent playlist command, used for accessing command specific
     * methods and generating error response messages.
     */
    protected final PluginCommand command;

    /**
     * Creates a new plugin sub command instance.
     *
     * @param avaire  The main avaire application instance.
     * @param command The parent plugin command instance.
     */
    public PluginSubCommand(AvaIre avaire, PluginCommand command) {
        this.avaire = avaire;
        this.command = command;
    }

    /**
     * Handles the sub plugin command using the given
     * command context and formatted arguments.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments parsed to the command.
     * @return {@code True} on success, {@code False} on failure.
     */
    public abstract boolean onCommand(CommandMessage context, String[] args);

    /**
     * Gets the plugin translator for the plugin with the given name.
     *
     * @param name The name of the plugin that should be returned.
     * @return The plugin translator instance for the matching plugin with
     *         the given name, or {@code NULL} if there were no match.
     */
    protected final Translator getPluginByName(String name) {
        List<PluginHolder> pluginHolders = avaire.getPluginManager().getOfficialPluginsList();
        if (pluginHolders == null) {
            return null;
        }

        for (PluginLoader pluginLoader : avaire.getPluginManager().getPlugins()) {
            if (pluginLoader.getName().equalsIgnoreCase(name)) {
                return new PluginLoaderTranslator(pluginLoader, avaire.getPluginManager().getOfficialPluginsList());
            }
        }

        for (PluginHolder holder : pluginHolders) {
            if (holder.getName().equalsIgnoreCase(name)) {
                return new PluginHolderTranslator(holder);
            }
        }

        return null;
    }
}
