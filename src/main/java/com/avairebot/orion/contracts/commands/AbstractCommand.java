package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Category;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.middleware.Middleware;
import com.avairebot.orion.permissions.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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

    public String generateDescription(Message message) {
        if (getMiddleware().isEmpty()) {
            return getDescription().trim();
        }

        List<String> description = new ArrayList<>();
        description.add(getDescription());
        description.add("");

        MIDDLEWARE_LOOP:
        for (String middleware : getMiddleware()) {
            String[] split = middleware.split(":");
            AtomicReference<Middleware> middlewareReference = new AtomicReference<>(Middleware.fromName(split[0]));

            switch (middlewareReference.get()) {
                case IS_BOT_ADMIN:
                    description.add("**You must be a Bot Administrator to use this command!**");
                    break MIDDLEWARE_LOOP;

                case THROTTLE:
                    String[] args = split[1].split(",");
                    description.add(String.format("**This command can only be used `%s` time(s) every `%s` seconds per %s**",
                            args[1], args[2], args[0].equalsIgnoreCase("guild") ? "server" : args[0]
                    ));
                    break;

                case HAS_ROLE:
                    String[] roles = split[1].split(",");
                    if (roles.length == 1) {
                        description.add(String.format("**The `%s` role is required to use this command!**", roles[0]));
                        break;
                    }
                    description.add(String.format("**The `%s` roles is required to use this command!**",
                            String.join("`, `", roles)
                    ));
                    break;

                case REQUIRE:
                    String[] nodes = split[1].split(",");
                    if (nodes.length == 1) {
                        description.add(String.format("**The `%s` permission is required to use this command!**",
                                Permissions.fromNode(nodes[0]).getPermission().getName()
                        ));
                        break;
                    }
                    description.add(String.format("**The `%s` permissions is required to use this command!**",
                            Arrays.asList(nodes).stream()
                                    .map(Permissions::fromNode)
                                    .map(Permissions::getPermission)
                                    .map(Permission::getName)
                                    .collect(Collectors.joining("`, `"))
                    ));
                    break;
            }
        }

        return description.stream().collect(Collectors.joining("\n"));
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
