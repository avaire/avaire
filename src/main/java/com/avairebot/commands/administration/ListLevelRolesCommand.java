package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListLevelRolesCommand extends Command {

    public ListLevelRolesCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "List Level Roles Command";
    }


    @Override
    public String getDescription() {
        return "List all the leveling roles and the level require to get them.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command` - Lists all the roles you can get for leveling up."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddLevelRoleCommand.class,
            RemoveLevelRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("llr");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList(
            "throttle:channel,1,5"
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

        if (transformer.getLevelRoles().isEmpty()) {
            context.makeInfo("There are currently no level roles, an administrator can add roles to the level up table using the `:command` command")
                .set("command", CommandHandler.getCommand(AddLevelRoleCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage()))
                .queue();

            return false;
        }

        ArrayList<Integer> keys = new ArrayList<>(
            transformer.getLevelRoles().keySet()
        );

        Collections.sort(keys);

        SimplePaginator paginator = new SimplePaginator(keys, 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> {
            //noinspection SuspiciousMethodCalls
            messages.add(
                String.format("`%s` => `%s`", val,
                    context.getGuild().getRoleById(
                        transformer.getLevelRoles().get(val)
                    ).getName()
                )
            );
        });

        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));

        context.makeSuccess(String.join("\n", messages))
            .setTitle(String.format("List of Level Roles (%s)", paginator.getTotal()))
            .requestedBy(context.getMember())
            .queue();

        return false;
    }
}
