package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.factories.MessageFactory;
import com.avairebot.permissions.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RequirePermissionMiddleware extends Middleware {

    public RequirePermissionMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String buildHelpDescription(@Nonnull String[] arguments) {
        arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        if (arguments.length == 1) {
            Permissions node = Permissions.fromNode(arguments[0]);
            if (node != null) {
                return String.format("**The `%s` permission is required to use this command!**",
                    node.getPermission().getName()
                );
            }
            return null;
        }
        return String.format("**The `%s` permissions is required to use this command!**",
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

        boolean isUserAdmin = message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission());
        RequireType type = RequireType.fromName(args[0]);

        List<Permissions> missingBotPermissions = new ArrayList<>();
        List<Permissions> missingUserPermissions = new ArrayList<>();

        for (String permissionNode : Arrays.copyOfRange(args, 1, args.length)) {
            Permissions permission = Permissions.fromNode(permissionNode);
            if (permission == null) {
                AvaIre.getLogger().warn(String.format("Invalid permission node given for the \"%s\" command: %s", stack.getCommand().getName(), permissionNode));
                return false;
            }

            if (!isUserAdmin && type.isCheckUser() && !message.getMember().hasPermission(permission.getPermission())) {
                missingUserPermissions.add(permission);
            }

            if (type.isCheckBot()) {
                if (!message.getGuild().getSelfMember().hasPermission(permission.getPermission())) {
                    missingBotPermissions.add(permission);
                    continue;
                }

                if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), permission.getPermission())) {
                    missingBotPermissions.add(permission);
                }
            }
        }

        if (!missingUserPermissions.isEmpty()) {
            MessageFactory.makeError(message, "You're missing the required permission node for this command:\n`:permission`")
                .set("permission", missingUserPermissions.stream()
                    .map(Permissions::getPermission)
                    .map(Permission::getName)
                    .collect(Collectors.joining("`, `"))
                ).queue();
        }

        if (!missingBotPermissions.isEmpty()) {
            MessageFactory.makeError(message, "I'm missing the following permission to run this command successfully:\n`:permission`")
                .set("permission", missingBotPermissions.stream()
                    .map(Permissions::getPermission)
                    .map(Permission::getName)
                    .collect(Collectors.joining("`, `"))
                ).queue();
        }

        return missingBotPermissions.isEmpty() && missingUserPermissions.isEmpty() && stack.next();
    }

    private enum RequireType {
        USER(true, false), BOT(false, true), ALL(true, true);

        private final boolean checkUser;
        private final boolean checkBot;

        RequireType(boolean checkUser, boolean checkBot) {
            this.checkUser = checkUser;
            this.checkBot = checkBot;
        }

        public static RequireType fromName(String name) {
            for (RequireType type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return RequireType.USER;
        }

        public boolean isCheckUser() {
            return checkUser;
        }

        public boolean isCheckBot() {
            return checkBot;
        }
    }
}
