package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.RoleUtil;
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
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();

        if (args.length == 0) {
            sendCurrentAutoRole(context, transformer).queue();
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            return disableAutoRole(context, transformer);
        }

        String roleName = String.join(" ", args);
        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(), roleName);
        if (role == null) {
            context.makeWarning(":user Invalid role, I couldn't find any role called **:role**")
                .set("role", roleName)
                .queue();
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(context.getMember().getRoles(), role)) {
            context.makeWarning(
                ":user The **:role** role is positioned higher in the hierarchy than any role you have, you can't add roles with a higher ranking than you have."
            ).set("role", role.getName()).queue();
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(context.getGuild().getSelfMember().getRoles(), role)) {
            context.makeWarning(
                ":user The **:role** role is positioned higher in the hierarchy, I can't give/remove this role from users."
            ).set("role", role.getName()).queue();
            return false;
        }

        try {
            updateAutorole(transformer, context, role.getId());

            context.makeSuccess(":user **Auto assign role** on user join has been **enabled** and set to  **:role**")
                .set("role", role.getName())
                .queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }
        return true;
    }

    private boolean disableAutoRole(CommandMessage context, GuildTransformer transformer) {
        try {
            transformer.setAutorole(null);
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("autorole", null));

            context.makeWarning(":user **Auto assign role** on user join is now **disabled**.").queue();
        } catch (SQLException ex) {
            ex.printStackTrace();
            AvaIre.getLogger().error(ex.getMessage(), ex);
        }

        return true;
    }

    private PlaceholderMessage sendCurrentAutoRole(CommandMessage context, GuildTransformer transformer) {
        if (transformer.getAutorole() == null) {
            return context.makeWarning(":user **Auto assign role** on user join is currently **disabled**.");
        }

        Role role = context.getGuild().getRoleById(transformer.getAutorole());
        if (role == null) {
            try {
                updateAutorole(transformer, context, null);
            } catch (SQLException ex) {
                ex.printStackTrace();
                AvaIre.getLogger().error(ex.getMessage(), ex);
            }
            return context.makeWarning(":user **Auto assign role** on user join is currently **disabled**.");
        }

        return context.makeSuccess(":user The **auto assign role** is currently set to **:role**")
            .set("role", role.getName());
    }

    private void updateAutorole(GuildTransformer transformer, CommandMessage context, String value) throws SQLException {
        transformer.setAutorole(value);
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .update(statement -> statement.set("autorole", value));
    }
}
