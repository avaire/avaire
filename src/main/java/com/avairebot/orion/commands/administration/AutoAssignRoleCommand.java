package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.RestAction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class AutoAssignRoleCommand extends Command {

    public AutoAssignRoleCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Autorole Command";
    }

    @Override
    public String getDescription() {
        return "Automatically assigns a specified role to every user who joins the server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
                "`:command` - Displays the current auto assignable role if one is set.",
                "`:command <role>` - The role that should be auto assignable.",
                "`:command disable` - Disables the auto assignable role."
        );
    }

    @Override
    public String getExampleUsage() {
        return "`:command @Member`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("autorole", "aar");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
                "require:bot,general.manage_roles",
                "require:user,general.administrator",
                "throttle:guild,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, message);

        if (args.length == 0) {
            sendCurrentAutoRole(message, transformer).queue();
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            return disableAutoRole(message, transformer);
        }

        List<Role> roles = message.getGuild().getRolesByName(args[0], true);
        if (roles.isEmpty()) {
            MessageFactory.makeWarning(message, "<@%s> Invalid role, I couldn't find any role called **%s**",
                    message.getAuthor().getId(), args[0]
            ).queue();
            return false;
        }

        Role role = roles.get(0);
        if (!RoleUtil.isRoleHierarchyLower(message.getMember().getRoles(), role)) {
            MessageFactory.makeWarning(message,
                    "<@%s> The **%s** role is positioned higher in the hierarchy than any role you have, you can't add roles with a higher ranking than you have.",
                    message.getAuthor().getId(),
                    role.getName()
            ).queue();
            return false;
        }

        if (!RoleUtil.isRoleHierarchyLower(message.getGuild().getSelfMember().getRoles(), role)) {
            MessageFactory.makeWarning(message,
                    "<@%s> The **%s** role is positioned higher in the hierarchy, I can't give/remove this role from users.",
                    message.getAuthor().getId(),
                    role.getName()
            ).queue();
            return false;
        }

        try {
            updateAutorole(transformer, message, role.getId());

            MessageFactory.makeSuccess(message, "<@%s> **Auto assign role** on user join has been **enabled** and set to  **%s**",
                    message.getAuthor().getId(),
                    role.getName()
            ).queue();
        } catch (SQLException e) {
            e.printStackTrace();
            orion.logger.fatal(e);
        }
        return true;
    }

    private boolean disableAutoRole(Message message, GuildTransformer transformer) {
        try {
            transformer.setAutorole(null);
            orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", message.getGuild().getId())
                    .update(statement -> statement.set("autorole", null));

            MessageFactory.makeWarning(message, "<@%s> **Auto assign role** on user join is now **disabled**.", message.getAuthor().getId()).queue();
        } catch (SQLException e) {
            e.printStackTrace();
            orion.logger.fatal(e);
        }

        return true;
    }

    private RestAction<Message> sendCurrentAutoRole(Message message, GuildTransformer transformer) {
        if (transformer.getAutorole() == null) {
            return MessageFactory.makeWarning(message, "<@%s> **Auto assign role** on user join is currently **disabled**.", message.getAuthor().getId());
        }

        Role role = message.getGuild().getRoleById(transformer.getAutorole());
        if (role == null) {
            try {
                updateAutorole(transformer, message, null);
            } catch (SQLException e) {
                e.printStackTrace();
                orion.logger.fatal(e);
            }
            return MessageFactory.makeWarning(message, "<@%s> **Auto assign role** on user join is currently **disabled**.", message.getAuthor().getId());
        }

        return MessageFactory.makeSuccess(message, "<@%s> The **auto assign role** is currently set to **%s**",
                message.getAuthor().getId(), role.getName()
        );
    }

    private void updateAutorole(GuildTransformer transformer, Message message, String value) throws SQLException {
        transformer.setAutorole(value);
        orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", message.getGuild().getId())
                .update(statement -> statement.set("autorole", value));
    }
}
