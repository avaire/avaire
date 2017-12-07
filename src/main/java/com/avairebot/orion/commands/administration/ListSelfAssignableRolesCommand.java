package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.SimplePaginator;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListSelfAssignableRolesCommand extends Command {

    public ListSelfAssignableRolesCommand(Orion orion) {
        super(orion, false);
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
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lsar");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, message);

        if (transformer.getSelfAssignableRoles().isEmpty()) {
            MessageFactory.makeWarning(message, "There are currently no self-assignable roles for this server.").queue();
            return true;
        }

        ArrayList<String> items = new ArrayList<>(transformer.getSelfAssignableRoles().values());
        Collections.sort(items);

        SimplePaginator paginator = new SimplePaginator(items, 10, 1);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(String.format("**%s**", val)));
        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(message)));

        MessageFactory.makeSuccess(message, String.join("\n", messages))
            .setTitle(String.format("There are %s self-assignable roles", paginator.getTotal()))
            .queue();

        return true;
    }
}
