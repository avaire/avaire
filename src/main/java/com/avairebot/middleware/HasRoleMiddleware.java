package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import com.avairebot.permissions.Permissions;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.util.List;

public class HasRoleMiddleware extends Middleware {

    public HasRoleMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        if (arguments.length == 1) {
            return String.format("**The `%s` role is required to use this command!**", arguments[0]);
        }
        return String.format("**The `%s` roles is required to use this command!**",
            String.join("`, `", arguments)
        );
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return stack.next();
        }

        List<Role> roles = message.getMember().getRoles();
        for (String roleName : args) {
            if (!hasRole(roles, roleName)) {
                MessageFactory.makeError(message, "You don't have the required role to execute this command:\n`:role`")
                    .set("role", roleName)
                    .queue();
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
