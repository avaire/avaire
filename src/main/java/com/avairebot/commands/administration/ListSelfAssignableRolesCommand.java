package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
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
    public List<String> getTriggers() {
        return Collections.singletonList("lsar");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, message);

        if (transformer.getSelfAssignableRoles().isEmpty()) {
            MessageFactory.makeWarning(message, "There are currently no self-assignable roles for this server.").queue();
            return true;
        }

        ArrayList<String> items = new ArrayList<>(transformer.getSelfAssignableRoles().values());
        Collections.sort(items);

        SimplePaginator paginator = new SimplePaginator(items, 10);
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
