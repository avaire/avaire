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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JavaPlugin {

    private final Set<ListenerAdapter> eventListeners = new HashSet<>();

    private Orion orion;

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
        return CategoryHandler.addCategory(name, defaultPrefix);
    }

    /**
     * Registers database migration to the migration containers, the migrations
     * will be used in {@link Migrations#up() up()}, {@link Migrations#down() down()} and {@link Migrations#rollback(int) rollback(int)}
     * <p>
     * All migrations must follow the {@link com.avairebot.orion.contracts.database.migrations.Migration Migration contract}.
     *
     * @param migration the migration that should be registered
     * @see com.avairebot.orion.contracts.database.migrations.Migration
     */
    public void registerMigration(Migration migration) {
        orion.getDatabase().getMigrations().register(migration);
    }

    public void registerEventListener(ListenerAdapter listener) {
        eventListeners.add(listener);
    }

    Set<ListenerAdapter> getEventListeners() {
        return eventListeners;
    }

    public Orion getOrion() {
        return orion;
    }

    void setOrion(Orion orion) {
        this.orion = orion;
    }

    public List<OrionShard> getShards() {
        return orion.getShards();
    }

    public CacheManager getCache() {
        return orion.getCache();
    }

    public DatabaseManager getDatabase() {
        return orion.getDatabase();
    }

    public abstract void onEnable();

    public void onDisable() {
        // This method does nothing...
    }
}
