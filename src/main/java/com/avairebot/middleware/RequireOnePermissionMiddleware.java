package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import com.avairebot.middleware.permission.PermissionCheck;
import com.avairebot.middleware.permission.PermissionCommon;
import com.avairebot.permissions.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RequireOnePermissionMiddleware extends Middleware {

    public RequireOnePermissionMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        if (arguments.length == 1) {
            return PermissionCommon.formatWithOneArgument(arguments[0]);
        }

        return String.format("**One of the following permissions is required to use this command:\n `%s`**",
            Arrays.stream(arguments)
                .map(Permissions::fromNode)
                .map(Permissions::getPermission)
                .map(Permission::getName)
                .collect(Collectors.joining("`, `"))
        );
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (args.length < 2) {
            AvaIre.getLogger().warn(String.format(
                "\"%s\" is parsing invalid amount of arguments to the require middleware, 2 arguments are required.", stack.getCommand()
            ));
            return stack.next();
        }

        PermissionCheck permissionCheck = new PermissionCheck(message, args);
        if (!permissionCheck.check(stack)) {
            return false;
        }

        if (!permissionCheck.userHasAtleastOne()) {
            MessageFactory.makeError(message, "You must have at least one of the following permission nodes to use this command:\n`:permission`")
                .set("permission", permissionCheck.getMissingUserPermissions().stream()
                    .map(Permissions::getPermission)
                    .map(Permission::getName)
                    .collect(Collectors.joining("`, `"))
                ).queue();
        }

        if (!permissionCheck.botHasAtleastOne()) {
            MessageFactory.makeError(message, "I'm missing one of the following permission nodes to run this command:\n`:permission`")
                .set("permission", permissionCheck.getMissingBotPermissions().stream()
                    .map(Permissions::getPermission)
                    .map(Permission::getName)
                    .collect(Collectors.joining("`, `"))
                ).queue();
        }

        if (permissionCheck.getType().isCheckUser() && permissionCheck.getType().isCheckBot()) {
            return permissionCheck.userHasAtleastOne()
                && permissionCheck.botHasAtleastOne()
                && stack.next();
        } else if (permissionCheck.getType().isCheckUser()) {
            return permissionCheck.userHasAtleastOne()
                && stack.next();
        } else if (permissionCheck.getType().isCheckBot()) {
            return permissionCheck.botHasAtleastOne()
                && stack.next();
        }

        // This should never be hit, if we do hit
        // it we'll just kill the middleware.
        return false;
    }
}
