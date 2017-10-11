package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.Middleware;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.permissions.Permissions;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;

public class HasRole extends Middleware {

    public HasRole(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return stack.next();
        }

        List<Role> roles = message.getMember().getRoles();
        for (String roleName : args) {
            if (!hasRole(roles, roleName)) {
                MessageFactory.makeError(
                    message,
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
