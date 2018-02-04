package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.NumberUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListAliasesCommand extends Command {

    public ListAliasesCommand(AvaIre avaire) {
        super(avaire, false);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, context.getGuild());

        if (transformer.getAliases().isEmpty()) {
            return sendErrorMessage(context, "The server doesn't have any aliases right now, you can create one using the\n`%salias <alias> <command>` command",
                generateCommandPrefix(context.getMessage())
            );
        }

        SimplePaginator paginator = new SimplePaginator(transformer.getAliases(), 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(String.format("`%s` => `%s`", key, val)));
        messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));

        context.makeSuccess(String.join("\n", messages))
            .setTitle(String.format("List of Aliases (%s)", paginator.getTotal()))
            .queue();

        return true;
    }
}
