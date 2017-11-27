package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.contracts.middleware.Middleware;
import net.dv8tion.jda.core.entities.Message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

public class MiddlewareStack {

    private final Orion orion;
    private final Message message;
    private final CommandContainer command;
    private final List<MiddlewareContainer> middlewares = new ArrayList<>();
    private final boolean mentionableCommand;

    private int index = -1;

    public MiddlewareStack(Orion orion, Message message, CommandContainer command, boolean mentionableCommand) {
        this.orion = orion;
        this.message = message;
        this.command = command;
        this.mentionableCommand = mentionableCommand;

        middlewares.add(new MiddlewareContainer(com.avairebot.orion.middleware.Middleware.PROCESS_COMMAND));

        this.buildMiddlewareStack();
    }

    public MiddlewareStack(Orion orion, Message message, CommandContainer command) {
        this(orion, message, command, false);
    }

    private void buildMiddlewareStack() {
        List<String> middleware = command.getCommand().getMiddleware();
        if (middleware.isEmpty()) {
            return;
        }

        ListIterator middlewareIterator = middleware.listIterator(middleware.size());
        while (middlewareIterator.hasPrevious()) {
            String previous = (String) middlewareIterator.previous();
            String[] split = previous.split(":");

            AtomicReference<com.avairebot.orion.middleware.Middleware> middlewareReference = new AtomicReference<>(com.avairebot.orion.middleware.Middleware.fromName(split[0]));
            if (middlewareReference.get() == null) {
                continue;
            }

            if (split.length == 1) {
                middlewares.add(new MiddlewareContainer(middlewareReference.get()));
                continue;
            }

            middlewares.add(new MiddlewareContainer(middlewareReference.get(), split[1].split(",")));
        }
    }

    public boolean next() {
        if (index == -1) {
            index = middlewares.size();
        }

        Class[] arguments = new Class[1];
        arguments[0] = Orion.class;

        try {
            MiddlewareContainer middlewareContainer = middlewares.get(--index);
            Middleware middleware = (Middleware) middlewareContainer.getMiddleware().getInstance().getDeclaredConstructor(arguments).newInstance(orion);

            return middleware.handle(message, this, middlewareContainer.getArguments());
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            Orion.getLogger().error("Invalid middleware object parsed, failed to create a new instance!", ex);
        } catch (IllegalAccessException ex) {
            Orion.getLogger().error("An attempt was made to create a new middleware instance!", ex);
        }
        return false;
    }

    public Command getCommand() {
        return command.getCommand();
    }

    public CommandContainer getCommandContainer() {
        return command;
    }

    public boolean isMentionableCommand() {
        return mentionableCommand;
    }
}
