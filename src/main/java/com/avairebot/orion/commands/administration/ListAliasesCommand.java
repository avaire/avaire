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
import java.util.Arrays;
import java.util.List;

public class ListAliasesCommand extends Command {

    public ListAliasesCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Aliases Command";
    }

    @Override
    public String getDescription() {
        return "Lists all the existing command aliases.";
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
        return Arrays.asList("aliases", "aliaslist");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:channel,1,5",
            "require:user,general.manage_server"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, message.getGuild());

        if (transformer.getAliases().isEmpty()) {
            return sendErrorMessage(message, "The server doesn't have any aliases right now, you can create one using the\n`.alias <alias> <command>` command");
        }

        SimplePaginator paginator = new SimplePaginator(transformer.getAliases(), 10, 1);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(String.format("`%s` => `%s`", key, val)));
        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(message)));

        MessageFactory.makeSuccess(message, String.join("\n", messages))
            .setTitle(String.format("List of Aliases (%s)", paginator.getTotal()))
            .queue();

        return true;
    }
}
