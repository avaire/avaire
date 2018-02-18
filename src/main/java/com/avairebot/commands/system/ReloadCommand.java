package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.plugin.PluginLoader;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends SystemCommand {

    public ReloadCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Reload Configuration Command";
    }

    @Override
    public String getDescription() {
        return "Reloads the main configuration, and all the configs for loaded plugins.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("reload");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        avaire.getConfig().reloadConfig();

        for (PluginLoader loader : avaire.getPluginManager().getPlugins()) {
            loader.getClassLoader().getPlugin().reloadConfig();
        }

        context.makeSuccess("Configuration has been successfully reloaded!").queue();

        return true;
    }
}
