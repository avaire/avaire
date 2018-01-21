package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutoAssignRoleCommand extends Command {

    public AutoAssignRoleCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Member`");
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
        GuildTransformer transformer = GuildController.fetchGuild(avaire, message);

        if (args.length == 0) {
            sendCurrentAutoRole(message, transformer).queue();
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            return disableAutoRole(message, transformer);
        }

        List<Role> roles = message.getGuild().getRolesByName(String.join(" ", args), true);
        if (roles.isEmpty()) {
            MessageFactory.makeWarning(message, ":user Invalid role, I couldn't find any role called **:role**")
                .set("role", String.join(" ", args))
                .queue();
            return false;
        }

        Role role = roles.get(0);
        if (RoleUtil.isRoleHierarchyHigher(message.getMember().getRoles(), role)) {
            MessageFactory.makeWarning(message,
                ":user The **:role** role is positioned higher in the hierarchy than any role you have, you can't add roles with a higher ranking than you have."
            ).set("role", role.getName()).queue();
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(message.getGuild().getSelfMember().getRoles(), role)) {
            MessageFactory.makeWarning(message,
                ":user The **:role** role is positioned higher in the hierarchy, I can't give/remove this role from users."
            ).set("role", role.getName()).queue();
            return false;
        }

        try {
            updateAutorole(transformer, message, role.getId());

            MessageFactory.makeSuccess(message, ":user **Auto assign role** on user join has been **enabled** and set to  **:role**")
                .set("role", role.getName())
                .queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }
        return true;
    }

    private boolean disableAutoRole(Message message, GuildTransformer transformer) {
        try {
            transformer.setAutorole(null);
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", message.getGuild().getId())
                .update(statement -> statement.set("autorole", null));

            MessageFactory.makeWarning(message, ":user **Auto assign role** on user join is now **disabled**.").queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }

        return true;
    }

    private PlaceholderMessage sendCurrentAutoRole(Message message, GuildTransformer transformer) {
        if (transformer.getAutorole() == null) {
            return MessageFactory.makeWarning(message, ":user **Auto assign role** on user join is currently **disabled**.");
        }

        Role role = message.getGuild().getRoleById(transformer.getAutorole());
        if (role == null) {
            try {
                updateAutorole(transformer, message, null);
            } catch (SQLException ex) {
                ex.printStackTrace();
                AvaIre.getLogger().error(ex.getMessage(), ex);
            }
            return MessageFactory.makeWarning(message, ":user **Auto assign role** on user join is currently **disabled**.");
        }

        return MessageFactory.makeSuccess(message, ":user The **auto assign role** is currently set to **:role**")
            .set("role", role.getName());
    }

    private void updateAutorole(GuildTransformer transformer, Message message, String value) throws SQLException {
        transformer.setAutorole(value);
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", message.getGuild().getId())
            .update(statement -> statement.set("autorole", value));
    }
}
