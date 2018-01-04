package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.middleware.Middleware;
import net.dv8tion.jda.core.entities.Message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

public class MiddlewareStack {

    private final AvaIre avaire;
    private final Message message;
    private final CommandContainer command;
    private final List<MiddlewareContainer> middlewares = new ArrayList<>();
    private final boolean mentionableCommand;

    private int index = -1;

    public MiddlewareStack(AvaIre avaire, Message message, CommandContainer command, boolean mentionableCommand) {
        this.avaire = avaire;
        this.message = message;
        this.command = command;
        this.mentionableCommand = mentionableCommand;

        middlewares.add(new MiddlewareContainer(com.avairebot.middleware.Middleware.PROCESS_COMMAND));

        this.buildMiddlewareStack();

        middlewares.add(new MiddlewareContainer(com.avairebot.middleware.Middleware.IS_CATEGORY_ENABLED));
        middlewares.add(new MiddlewareContainer(com.avairebot.middleware.Middleware.INCREMENT_METRICS_FOR_COMMAND));
    }

    public MiddlewareStack(AvaIre avaire, Message message, CommandContainer command) {
        this(avaire, message, command, false);
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

            AtomicReference<com.avairebot.middleware.Middleware> middlewareReference = new AtomicReference<>(com.avairebot.middleware.Middleware.fromName(split[0]));
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
        arguments[0] = AvaIre.class;

        try {
            MiddlewareContainer middlewareContainer = middlewares.get(--index);
            Middleware middleware = (Middleware) middlewareContainer.getMiddleware().getInstance().getDeclaredConstructor(arguments).newInstance(avaire);

            return middleware.handle(message, this, middlewareContainer.getArguments());
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            AvaIre.getLogger().error("Invalid middleware object parsed, failed to create a new instance!", ex);
        } catch (IllegalAccessException ex) {
            AvaIre.getLogger().error("An attempt was made to create a new middleware instance!", ex);
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
