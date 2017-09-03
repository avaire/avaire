package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.AbstractMiddleware;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.permissions.Permissions;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class HasRole extends AbstractMiddleware {

    public HasRole(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(MessageReceivedEvent event, MiddlewareStack stack, String... args) {
        if (!event.getChannelType().isGuild()) {
            return stack.next();
        }

        if (event.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return stack.next();
        }

        List<Role> roles = event.getMessage().getMember().getRoles();
        for (String roleName : args) {
            if (!hasRole(roles, roleName)) {
                MessageFactory.makeError(
                        event.getMessage(),
                        "You don't have the required role to execute this command:\n`%s`",
                        roleName
                ).queue();
                return false;
            }

        }

        return stack.next();
    }

    private boolean hasRole(List<Role> roles, String roleName) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }
}
