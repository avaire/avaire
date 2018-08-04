package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RemoveLevelRoleCommand extends Command {

    public RemoveLevelRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Remove Level Roles Command";
    }

    @Override
    public String getDescription() {
        return "Remove a role from the leveling up role table.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role>` - Removes the role from the leveling up role table.",
            "`:command <level>` - Removes the role that is assigned to the given level from the role table."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddLevelRoleCommand.class,
            ListLevelRolesCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rlr");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null || !transformer.isLevels()) {
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument, the `role` argument is required.");
        }


        Role role = getRoleFromContext(context, transformer, args);
        if (role == null) {
            context.makeWarning(NumberUtil.isNumeric(args[0]) ?
                ":user There are no roles linked to level **:role** on the level up table." :
                ":user There are no role called **:role** on the level up table."
            ).set("role", String.join(" ", args)).queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(context.getMessage(), role)) {
            return false;
        }

        if (!transformer.getLevelRoles().containsValue(role.getId())) {
            context.makeWarning(":user Invalid role, the `:name` role is not on the level up table.")
                .set("name", role.getName())
                .queue();

            return false;
        }

        int level = -1;
        for (Map.Entry<Integer, String> entry : transformer.getLevelRoles().entrySet()) {
            if (entry.getValue().equals(role.getId())) {
                level = entry.getKey();
            }
        }

        try {
            transformer.getLevelRoles().remove(level);
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("level_roles", AvaIre.gson.toJson(transformer.getLevelRoles()), true);
                });

            context.makeSuccess("Role **:role** role has been removed from the level-up role list.\nThe server now have `:slots` level role slots available.")
                .set("slots", transformer.getType().getLimits().getLevelRoles() - transformer.getLevelRoles().size())
                .set("role", role.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Role getRoleFromContext(CommandContext context, GuildTransformer transformer, String[] args) {
        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(), String.join(" ", args));
        if (role != null) {
            return role;
        }

        int roleLevel = NumberUtil.parseInt(args[0], 0);
        if (transformer.getLevelRoles().containsKey(roleLevel)) {
            return context.getGuild().getRoleById(
                transformer.getLevelRoles().get(roleLevel)
            );
        }

        return null;
    }
}
