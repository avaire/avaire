package com.avairebot.orion.plugin;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheManager;
import com.avairebot.orion.commands.CategoryHandler;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.shard.OrionShard;

import java.util.List;

public abstract class JavaPlugin {

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
