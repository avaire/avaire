package com.avairebot.orion.plugin;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheManager;
import com.avairebot.orion.commands.CategoryHandler;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.contracts.database.migrations.Migration;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.migrate.Migrations;
import com.avairebot.orion.shard.OrionShard;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JavaPlugin {

    private final Set<ListenerAdapter> eventListeners = new HashSet<>();

    private Logger logger = LoggerFactory.getLogger(JavaPlugin.class);
    private Orion orion;

    /**
     * Initializes the plugin by setting the global orion
     * application instance and preparing some data.
     *
     * @param orion The global orion application instance.
     */
    final void init(Orion orion) {
        this.orion = orion;
        this.logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
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
     * Register the given command into the command handler, creating the
     * command container and saving it into the commands collection.
     *
     * @param command The command that should be registered into the command handler.
     */
    public void registerCommand(Command command) {
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
    public boolean registerCategory(String name, String defaultPrefix) {
        return CategoryHandler.addCategory(orion, name, defaultPrefix);
    }

    /**
     * Registers database migration to the migration containers, the migrations
     * will be used in {@link Migrations#up() up()}, {@link Migrations#down() down()} and {@link Migrations#rollback(int) rollback(int)}
     * <p>
     * All migrations must follow the {@link com.avairebot.orion.contracts.database.migrations.Migration Migration contract}.
     *
     * @param migration The migration that should be registered
     * @see com.avairebot.orion.contracts.database.migrations.Migration
     */
    public void registerMigration(Migration migration) {
        orion.getDatabase().getMigrations().register(migration);
    }

    /**
     * Registers a JDA event listener, the class must extend from the {@link ListenerAdapter Abstract Listener Adapter}.
     *
     * @param listener The event listener that should be registered.
     */
    public void registerEventListener(ListenerAdapter listener) {
        eventListeners.add(listener);
    }

    /**
     * Returns the global Orion application instance.
     *
     * @return The global Orion application instance.
     */
    public Orion getOrion() {
        return orion;
    }

    /**
     * Gets a list of all the shard instances of the bot, the
     * list will always have at least one entry.
     *
     * @return A list of bot shard entries.
     */
    public List<OrionShard> getShards() {
        return orion.getShards();
    }

    /**
     * Gets the application cache manager, giving you
     * access to store things in files or in memory.
     *
     * @return The application cache manager.
     */
    public CacheManager getCache() {
        return orion.getCache();
    }

    /**
     * Gets the application database manager, giving you access to query the database, create migrations, and change the schema.
     *
     * @return The application database manager.
     */
    public DatabaseManager getDatabase() {
        return orion.getDatabase();
    }

    /**
     * Returns the plugin logger associated with the application logger. The returned
     * logger automatically tags all log messages with the plugin's name.
     *
     * @return Logger associated with this plugin.
     */
    public Logger getLogger() {
        return logger;
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
