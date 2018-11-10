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

package com.avairebot.plugin;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheManager;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandHandler;
import com.avairebot.config.Configuration;
import com.avairebot.config.YamlConfiguration;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.migrate.Migrations;
import com.avairebot.exceptions.InvalidConfigurationException;
import com.avairebot.language.I18n;
import com.avairebot.language.Language;
import com.avairebot.language.LanguageContainer;
import com.avairebot.middleware.MiddlewareHandler;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public abstract class JavaPlugin {

    private final Set<ListenerAdapter> eventListeners = new HashSet<>();

    private Logger log = LoggerFactory.getLogger(JavaPlugin.class);

    private AvaIre avaire;
    private PluginLoader loader;

    private Configuration config = null;

    /**
     * Initializes the plugin by setting the global avaire
     * application instance and preparing some data.
     *
     * @param avaire The global avaire application instance.
     */
    final void init(AvaIre avaire, PluginLoader loader) {
        this.avaire = avaire;
        this.loader = loader;
        this.log = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    /**
     * Gets a set of the event listeners that has been registered by the plugin.
     *
     * @return A set of registered event listeners.
     */
    final Set<ListenerAdapter> getEventListeners() {
        return eventListeners;
    }

    /**
     * Registers a middleware with the given name, middlewares can be used through
     * the {@link Command#getMiddleware() getMiddleware()} method, middleware
     * names will ignore letter casing and there can't be two middlewares
     * registered with the same name at the same time.
     *
     * @param name       The name of the middleware.
     * @param middleware The middleware instance that should be linked to the given name.
     * @throws IllegalArgumentException This is thrown if a middleware is already registered with the given name.
     */
    public final void registerMiddleware(String name, Middleware middleware) {
        MiddlewareHandler.register(name, middleware);
    }

    /**
     * Register the given command into the command handler, creating the
     * command container and saving it into the commands collection.
     *
     * @param command The command that should be registered into the command handler.
     */
    public final void registerCommand(Command command) {
        CommandHandler.register(command);
    }

    /**
     * Register the given category name if it doesn't already exists with the
     * given default prefix, once the category has been registered the
     * category will show up on the help command, and be available
     * to users for customization via the prefix command.
     *
     * @param name          The name of the category that should be registered, can not contain spaces or special characters.
     * @param defaultPrefix The default prefix for the category.
     * @return <code>True</code> on success, <code>False</code> if the category already exists.
     */
    public final boolean registerCategory(String name, String defaultPrefix) {
        return CategoryHandler.addCategory(avaire, name, defaultPrefix);
    }

    /**
     * Registers database migration to the migration containers, the migrations
     * will be used in {@link Migrations#up() up()}, {@link Migrations#down() down()} and {@link Migrations#rollback(int) rollback(int)}
     * <p>
     * All migrations must follow the {@link com.avairebot.contracts.database.migrations.Migration Migration contract}.
     *
     * @param migration The migration that should be registered
     * @see com.avairebot.contracts.database.migrations.Migration
     */
    public final void registerMigration(Migration migration) {
        avaire.getDatabase().getMigrations().register(migration);
    }

    /**
     * Registers a JDA event listener, the class must extend from the {@link ListenerAdapter Abstract Listener Adapter}.
     *
     * @param listener The event listener that should be registered.
     */
    public final void registerEventListener(ListenerAdapter listener) {
        eventListeners.add(listener);
    }

    /**
     * Registers an I18n input stream, the input stream will be parsed to the
     * {@link YamlConfiguration#loadConfiguration(Reader)} method to be
     * parsed to a YAML object, to then be merged with the default
     * language file, only keys that doesn't already exists in
     * the default language file will be added.
     * <p>
     * An example of how to register a English yaml file for a plugin can be found below:
     * <br>
     * <pre><code>
     * registerI18n(
     *     Language.EN_US,
     *     getPluginLoader().getResource("langs/English.yml")
     * );
     * </code></pre>
     *
     * @param language The language that the input stream should be merged with.
     * @param file     The file input stream that should be merged with the given language.
     */
    public final void registerI18n(Language language, @Nonnull InputStream file) {
        Checks.notNull(file, "file input stream");
        mergeConfiguration(language, YamlConfiguration.loadConfiguration(
            new InputStreamReader(file)
        ));
    }

    /**
     * Registers an I18n string, the string will be parsed through the
     * {@link YamlConfiguration#loadFromString(String)} methods to be
     * parsed to a YAML object, to then be merged with the default
     * language file, only keys that doesn't already exists in
     * the default language file will be added.
     *
     * @param language The language that the input stream should be merged with.
     * @param yaml     The YAML as a string that should be parsed and merged with the given language.
     * @throws InvalidConfigurationException Thrown if the given string can not be parsed correctly to a YAML object.
     */
    public final void registerI18n(Language language, @Nonnull String yaml) throws InvalidConfigurationException {
        YamlConfiguration pluginConfig = new YamlConfiguration();
        pluginConfig.loadFromString(yaml);
        mergeConfiguration(language, pluginConfig);
    }

    private void mergeConfiguration(Language language, YamlConfiguration pluginConfig) {
        LanguageContainer locale = I18n.getLocale(language);
        for (String key : pluginConfig.getKeys(true)) {
            if (!locale.getConfig().contains(key)) {
                locale.getConfig().set(key, pluginConfig.get(key));
            }
        }
    }

    /**
     * Returns the global AvaIre application instance.
     *
     * @return The global AvaIre application instance.
     */
    public final AvaIre getAvaire() {
        return avaire;
    }

    /**
     * Gets the shard manager used by the bot, giving you access to methods
     * that can interact with the application on a global scale.
     *
     * @return A list of bot shard entries.
     */
    public final ShardManager getShardManager() {
        return avaire.getShardManager();
    }

    /**
     * Gets the application cache manager, giving you
     * access to store things in files or in memory.
     *
     * @return The application cache manager.
     */
    public final CacheManager getCache() {
        return avaire.getCache();
    }

    /**
     * Gets the application database manager, giving you access to query the database, create migrations, and change the schema.
     *
     * @return The application database manager.
     */
    public final DatabaseManager getDatabase() {
        return avaire.getDatabase();
    }

    /**
     * Loads the default {@link Configuration config} into memory and prepares
     * it for the plugin, if the config has already been loaded the
     * {@link Configuration config} will just be returned.
     *
     * @return The {@link Configuration config} that.
     */
    public final Configuration getConfig() {
        if (config == null) {
            try {
                config = new Configuration(
                    this,
                    new File(
                        loader.getDataFolder(),
                        loader.getName()
                    ),
                    "config.yml"
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    /**
     * Saves the {@link Configuration config} file from {@link #getConfig()}
     * to disk under the name "config.yml", if the config file already
     * exists this method will fail silently.
     */
    public final void saveDefaultConfig() {
        if (getConfig() != null) {
            getConfig().saveDefaultConfig();
        }
    }

    /**
     * Discards any data in {@link #getConfig()} and reloads from disk.
     */
    public final void reloadConfig() {
        if (getConfig() != null) {
            getConfig().reloadConfig();
        }
    }

    /**
     * Returns the plugin logger associated with the application logger. The returned
     * logger automatically tags all log messages with the plugin's name.
     *
     * @return Logger associated with this plugin.
     */
    public final Logger getLogger() {
        return log;
    }

    /**
     * Gets the {@link PluginLoader plugin loader} used for loading this plugin, the
     * {@link PluginLoader plugin loader} can be used to access data from
     * the "plugin.yml" file for this plugin.
     *
     * @return The {@link PluginLoader plugin loader} used to load this plugin.
     */
    public final PluginLoader getPluginLoader() {
        return loader;
    }

    /**
     * Called when this plugin is enabled
     */
    public abstract void onEnable();

    /**
     * Called when this plugin is disabled
     */
    public void onDisable() {
        // This method does nothing...
    }
}
