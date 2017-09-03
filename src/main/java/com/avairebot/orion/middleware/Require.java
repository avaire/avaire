package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.permissions.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Require extends AbstractMiddleware {

    public Require(Orion orion) {
        super(orion);
    }

    @Override
    public void handle(MessageReceivedEvent event, MiddlewareStack stack, String... args) {
        if (!event.getChannelType().isGuild()) {
            stack.next();
            return;
        }

        if (event.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            stack.next();
            return;
        }
        
        List<Permissions> missingPermissions = new ArrayList<>();

        for (String permissionNode : args) {
            Permissions permission = Permissions.fromNode(permissionNode);
            if (permission == null) {
                orion.logger.warning("Invalid permission node given for the \"%s\" command: %s", stack.getCommand().getName(), permissionNode);
                return;
            }

            if (!event.getMember().hasPermission(permission.getPermission())) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            MessageFactory.makeError(
                    event.getMessage(),
                    "You're missing the required permission node for this command:\n`%s`",
                    missingPermissions.stream()
                            .map(Permissions::getPermission)
                            .map(Permission::getName)
                            .collect(Collectors.joining("`, `"))
            ).queue();
            return;
        }

        stack.next();
    }
}
