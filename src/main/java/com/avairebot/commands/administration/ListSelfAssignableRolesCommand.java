package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListSelfAssignableRolesCommand extends Command {

    public ListSelfAssignableRolesCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "List Self Assignable Roles Command";
    }

    @Override
    public String getDescription() {
        return "List all the self-assignable roles, 10 per-page.";
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddSelfAssignableRoleCommand.class,
            RemoveSelfAssignableRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lsar");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (transformer.getSelfAssignableRoles().isEmpty()) {
            context.makeWarning("There are currently no self-assignable roles for this server.").queue();
            return true;
        }

        ArrayList<String> items = new ArrayList<>();
        for (String roleId : transformer.getSelfAssignableRoles().keySet()) {
            Role roleById = context.getGuild().getRoleById(roleId);
            if (roleById != null) {
                items.add(roleById.getName());
            }
        }

        Collections.sort(items);
        SimplePaginator paginator = new SimplePaginator(items, 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(String.format("**%s**", val)));
        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));

        context.makeSuccess(String.join("\n", messages))
            .setTitle(String.format("There are %s self-assignable roles", paginator.getTotal()))
            .requestedBy(context.getMember())
            .queue();

        return true;
    }
}
