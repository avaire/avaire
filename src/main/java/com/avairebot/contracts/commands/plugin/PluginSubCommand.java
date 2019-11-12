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
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.system.PluginCommand;
import com.avairebot.contracts.plugin.Plugin;
import com.avairebot.contracts.plugin.PluginAsset;
import com.avairebot.contracts.plugin.PluginRelease;

import java.sql.SQLException;

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
     * Creates a new plugin index using the given plugin, release, and asset,
     * this will create a new record in the database with the related
     * information for the given plugin release and the assets
     * associated with the downloaded file, allowing the bot
     * to re-install the same version of the plugin later
     * if it becomes necessary.
     *
     * @param plugin  The plugin that the index should be created for.
     * @param release The plugin release that the index should be created with.
     * @param asset   The asset that should be associated with the index.
     * @throws SQLException Thrown if the database failed to write the record to the database.
     */
    protected final void createPluginIndex(Plugin plugin, PluginRelease release, PluginAsset asset) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.INSTALLED_PLUGINS_TABLE_NAME)
            .insert(statement -> {
                statement.set("name", plugin.getName());
                statement.set("version", release.getTag());
                statement.set("source", plugin.getRepository().getSource().getName());
                statement.set("repository", plugin.getRepository().getRepository());
                statement.set("download_url", asset.getDownloadableUrl());
            });
    }

    /**
     * Deletes all plugin indexes related to the given plugin,
     * removing them from the plugin index altogether.
     *
     * @param plugin The plugin that should be removed from the plugin index.
     * @throws SQLException Thrown if the database failed to delete the record from the database.
     */
    protected final void deletePluginIndex(Plugin plugin) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.INSTALLED_PLUGINS_TABLE_NAME)
            .where("name", plugin.getName())
            .where("source", plugin.getRepository().getSource().getName())
            .where("repository", plugin.getRepository().getRepository())
            .delete();
    }
}
