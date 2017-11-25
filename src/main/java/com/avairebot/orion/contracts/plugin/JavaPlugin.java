package com.avairebot.orion.contracts.plugin;

public abstract class JavaPlugin {

    public abstract void onEnable();

    public void onDisable() {
        // This method does nothing...
    }
}
