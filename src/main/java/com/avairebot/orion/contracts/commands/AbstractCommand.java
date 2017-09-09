package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Category;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCommand {
    protected final Orion orion;
    protected final boolean allowDM;

    public AbstractCommand(Orion orion) {
        this(orion, false);
    }

    public AbstractCommand(Orion orion, boolean allowDM) {
        this.orion = orion;
        this.allowDM = allowDM;
    }

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<String> getUsageInstructions();

    public abstract String getExampleUsage();

    public abstract List<String> getTriggers();

    public List<String> getMiddleware() {
        return new ArrayList<>();
    }

    public boolean isAllowedInDM() {
        return allowDM;
    }

    public abstract boolean onCommand(Message message, String[] args);

    protected boolean sendErrorMessage(Message message, String error) {
        Category category = Category.fromCommand(this);

        message.getChannel().sendMessage(MessageFactory.createEmbeddedBuilder()
                .setTitle(getName())
                .setDescription(error)
                .setColor(MessageFactory.MessageType.ERROR.getColor())
                .addField("Usage", getUsageInstructions().stream().collect(Collectors.joining("\n")), false)
                .addField("Example Usage", getExampleUsage(), false)
                .setFooter("Command category: " + category.getName(), null).build()).queue();

        return false;
    }
}
