package com.avairebot.commands;

import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.middleware.MiddlewareHandler;
import com.avairebot.middleware.ThrottleMiddleware;

import java.util.ArrayList;
import java.util.List;

public class CommandContainer {

    private final Command command;
    private final Category category;
    private final String sourceUri;
    private final List<String> middlewares;

    public CommandContainer(Command command, Category category, String sourceUri) {
        this.command = command;
        this.category = category;
        this.sourceUri = sourceUri;

        this.middlewares = new ArrayList<>(command.getMiddleware());

        if (!hasMiddleware(ThrottleMiddleware.class)) {
            middlewares.add("throttle:user,3,2");
        }
    }

    public Command getCommand() {
        return command;
    }

    public Category getCategory() {
        return category;
    }

    public String getDefaultPrefix() {
        return category.getPrefix();
    }

    public CommandPriority getPriority() {
        return command.getCommandPriority();
    }

    public List<String> getMiddleware() {
        return middlewares;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    private boolean hasMiddleware(Class<? extends Middleware> clazz) {
        String key = MiddlewareHandler.getName(clazz);
        if (key == null) {
            return false;
        }

        for (String middleware : middlewares) {
            if (middleware.toLowerCase().startsWith(key)) {
                return true;
            }
        }
        return false;
    }
}
