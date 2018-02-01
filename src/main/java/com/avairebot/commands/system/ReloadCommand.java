package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

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
    public boolean onCommand(Message message, String[] args) {
        avaire.getConfig().reloadConfig();

        // TODO: When plugins have configuration support, add a loop here to reload the configs.

        MessageFactory.makeSuccess(message, "Configuration has been successfully reloaded!").queue();

        return true;
    }
}
