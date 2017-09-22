package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Category;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractCommand {
    protected final Orion orion;
    protected final boolean allowDM;

    public AbstractCommand(Orion orion) {
        this(orion, true);
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
                .addField("Usage", generateUsageInstructions(message), false)
                .addField("Example Usage", generateExampleUsage(message), false)
                .setFooter("Command category: " + category.getName(), null).build()).queue();

        return false;
    }

    public String generateUsageInstructions(Message message) {
        return formatCommandGeneratorString(message,
                getUsageInstructions() == null ? "`:command`" :
                        getUsageInstructions().stream()
                                .collect(Collectors.joining("\n"))
        );
    }

    public String generateExampleUsage(Message message) {
        return formatCommandGeneratorString(message,
                getExampleUsage() == null ? "`:command`" : getExampleUsage()
        );
    }

    public boolean isSame(AbstractCommand command) {
        return Objects.equals(command.getName(), getName())
                && Objects.equals(command.getDescription(), command.getDescription())
                && Objects.equals(command.getUsageInstructions(), getUsageInstructions())
                && Objects.equals(command.getExampleUsage(), getExampleUsage())
                && Objects.equals(command.getTriggers(), getTriggers());
    }

    private String formatCommandGeneratorString(Message message, String string) {
        CommandContainer container = CommandHandler.getCommand(this);
        String command = container.getDefaultPrefix() + container.getCommand().getTriggers().get(0);

        return string.replaceAll(":command", command);
    }
}
