package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddLevelRoleCommand extends Command {

    public AddLevelRoleCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Add Level Role Command";
    }

    @Override
    public String getDescription() {
        return "Adds a role to the leveling up table, roles on the table will be given to users once they level up and meet the requirements for the role.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <level requirement> <role>` - Adds to role to users when they level up and meet the level requirement."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 5 Regular` - Adds the Regular role to the level up table, users who are level 5 and up will get the role when they level up."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ListLevelRolesCommand.class,
            RemoveLevelRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("alr");
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

        if (transformer.getLevelRoles().size() >= transformer.getType().getLimits().getLevelRoles()) {
            context.makeWarning("The server doesn't have any more level role slots, you can remove existing level roles to free up slots.")
                .queue();

            return false;
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument, the `level requirement` argument is required.");
        }

        int level = NumberUtil.parseInt(args[0], 0);
        if (level < 1) {
            context.makeWarning("Invalid level requirement given, the level requirement must be a positive number.")
                .queue();

            return false;
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "Missing argument, the `role` argument is required.");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(),
            String.join(" ", Arrays.copyOfRange(args, 1, args.length))
        );

        if (role == null) {
            context.makeWarning(":user Invalid role, I couldn't find any role called **:role**")
                .set("role", String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
                .queue();
            return false;
        }

        if (transformer.getLevelRoles().containsValue(role.getId())) {
            context.makeWarning(":user The **:role** is already a level role, you can't use the same role twice.")
                .set("role", role.getName())
                .queue();
            return false;
        }

        if (!RoleUtil.canInteractWithRole(context.getMessage(), role)) {
            return false;
        }

        if (transformer.getLevelRoles().containsKey(level)) {
            Role roleById = context.getGuild().getRoleById(
                transformer.getLevelRoles().get(level)
            );

            if (roleById != null) {
                context.makeWarning("There is already a role assigned to level **:level**, only one role can be given per level.")
                    .set("level", level)
                    .queue();

                return false;
            }
        }

        try {
            transformer.getLevelRoles().put(level, role.getId());
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("level_roles", AvaIre.gson.toJson(transformer.getLevelRoles()), true);
                });

            context.makeSuccess("Role **:role** role has been added to the level-up role list.\nThe server has `:slots` more level role slots available.")
                .set("slots", transformer.getType().getLimits().getLevelRoles() - transformer.getLevelRoles().size())
                .set("role", role.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
