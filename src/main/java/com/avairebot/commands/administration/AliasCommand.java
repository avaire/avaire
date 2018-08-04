package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AliasCommand extends Command {

    public AliasCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command !ava \"repeat **Website:** https://avairebot.com/ \"`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(ListAliasesCommand.class);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument, the `alias` argument is required.");
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "An error occurred while loading the server settings, please try again, if the problem continues please report this to one of my developers on the [AvaIre support server](https://discord.gg/gt2FWER).");
        }

        if (args.length == 1) {
            return removeCustomAlias(context, transformer, args);
        }

        if (transformer.getAliases().containsKey(args[0].toLowerCase())) {
            return sendErrorMessage(context, "There is already a custom alias called `{0}`", args[0]);
        }

        if (transformer.getAliases().size() >= transformer.getType().getLimits().getAliases()) {
            context.makeWarning("The server doesn't have any more alias slots, you can delete existing aliases to free up slots.").queue();
            return false;
        }

        String alias = args[0].toLowerCase();
        String[] split = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).split(" ");
        CommandContainer command = CommandHandler.getCommand(context.getMessage(), split[0]);
        if (command == null) {
            return sendErrorMessage(context, "Invalid command given, I don't know of any command called `{0}`",
                split[0]
            );
        }

        String commandString = command.getDefaultPrefix()
            + command.getCommand().getTriggers().get(0) + " "
            + String.join(" ", Arrays.copyOfRange(split, 1, split.length));

        transformer.getAliases().put(alias, commandString);

        try {
            updateGuildAliases(context, transformer);

            context.makeSuccess("The `:alias` alias has been linked to `:command`\nThe server has `:slots` more aliases slots available.")
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

    private boolean removeCustomAlias(CommandMessage context, GuildTransformer transformer, String[] args) {
        if (!transformer.getAliases().containsKey(args[0].toLowerCase())) {
            return sendErrorMessage(context, "Invalid alias given, `{0}` is not registered as an alias.", args[0]);
        }

        transformer.getAliases().remove(args[0].toLowerCase());

        try {
            updateGuildAliases(context, transformer);

            context.makeSuccess("The `:alias` alias has been deleted successfully.")
                .set("alias", args[0])
                .queue();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateGuildAliases(CommandMessage message, GuildTransformer transformer) throws SQLException {
        avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", message.getGuild().getId())
            .update(statement -> statement.set("aliases", AvaIre.gson.toJson(transformer.getAliases()), true));
    }
}
