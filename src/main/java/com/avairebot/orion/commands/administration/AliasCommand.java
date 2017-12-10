package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class AliasCommand extends Command {

    public AliasCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Alias Command";
    }

    @Override
    public String getDescription() {
        return "Creates and maps a custom alias for a pre-existing command. Provide no alias to remove an existing alias.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <alias>` - Deletes the alias if it exists.",
            "`:command <alias> <command>` - Creates an alias for the given command."
        );
    }

    @Override
    public String getExampleUsage() {
        return "`:command !ava \">repeat **Website:** https://avairebot.com/ \"`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("alias", "cmdmap");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:user,2,5",
            "require:user,general.manage_server"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument, the `alias` argument is required.");
        }

        GuildTransformer transformer = GuildController.fetchGuild(orion, message.getGuild());
        if (transformer == null) {
            return sendErrorMessage(message, "An error occurred while loading the server settings, please try again, if the problem continues please report this to one of my developers on the [AvaIre support server](https://discord.gg/gt2FWER).");
        }

        if (args.length == 1) {
            return removeCustomAlias(message, transformer, args);
        }

        if (transformer.getAliases().containsKey(args[0].toLowerCase())) {
            return sendErrorMessage(message, "There is already a custom alias called `%s`", args[0]);
        }

        if (transformer.getAliases().size() >= transformer.getType().getLimits().getAliases()) {
            MessageFactory.makeWarning(message, "The server doesn't have any more alias slots, you can delete existing aliases to free up slots.").queue();
            return false;
        }

        String alias = args[0].toLowerCase();
        String[] split = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).split(" ");
        CommandContainer command = CommandHandler.getCommand(message, split[0]);
        if (command == null) {
            return sendErrorMessage(message, "Invalid command given, I don't know of any command called `%s`",
                split[0]
            );
        }

        String commandString = command.getDefaultPrefix()
            + command.getCommand().getTriggers().get(0) + " "
            + String.join(" ", Arrays.copyOfRange(split, 1, split.length));

        transformer.getAliases().put(alias, commandString);

        try {
            updateGuildAliases(message, transformer);
            MessageFactory.makeSuccess(message, "The `:alias` alias has been linked to `:command`\nThe server has `:slots` more aliases slots available.")
                .set("alias", args[0])
                .set("command", commandString)
                .set("slots", transformer.getType().getLimits().getAliases() - transformer.getAliases().size())
                .queue();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeCustomAlias(Message message, GuildTransformer transformer, String[] args) {
        if (!transformer.getAliases().containsKey(args[0].toLowerCase())) {
            return sendErrorMessage(message, "Invalid alias given, `%s` is not registered as an alias.", args[0]);
        }

        transformer.getAliases().remove(args[0].toLowerCase());

        try {
            updateGuildAliases(message, transformer);
            MessageFactory.makeSuccess(message, "The `:alias` alias has been deleted successfully.")
                .set("alias", args[0])
                .queue();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateGuildAliases(Message message, GuildTransformer transformer) throws SQLException {
        orion.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", message.getGuild().getId())
            .update(statement -> statement.set("aliases", new GsonBuilder().disableHtmlEscaping().create()
                .toJson(transformer.getAliases())));
    }
}
