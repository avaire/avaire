package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.Category;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import com.google.gson.Gson;
import net.dv8tion.jda.core.entities.Message;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Change Prefix Command";
    }

    @Override
    public String getDescription() {
        return "Sets the prefix that should be used for all commands in a given category, if no prefix is provided the category prefix will be reset back to the default instead.\n" +
                "**Note:** Command prefixes cannot contain spaces and if multiple categories are using the same prefix, some commands triggers may not run the indented command since multiple commands share the same triggers but has a different prefix by default.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
                "`:command <category>` - Resets the category prefix back to its default prefix.",
                "`:command <category> [prefix]` - Sets the category prefix to the given prefix.");
    }

    @Override
    public String getExampleUsage() {
        return String.join("\n", Arrays.asList(
                "`:command fun`",
                "`:command admin /`",
                "`:command utility %`"
        ));
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("changeprefix", "prefix");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
                "require:user,general.administrator",
                "throttle:guild,1,5"
        );
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIGHEST;
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument `category`, you must specify the command category you want to change/reset the prefix for.");
        }

        Category category = Category.fromLazyName(args[0]);
        if (category == null) {
            return sendErrorMessage(message, "Invalid `category` given, there are no command categories that are called, or starts with `%s`", args[0]);
        }

        GuildTransformer transformer = GuildController.fetchGuild(orion, message);
        if (args.length == 1) {
            return removeCustomPrefix(message, transformer, category);
        }

        String prefix = args[1];
        if (prefix.contains(" ") || prefix.length() < 1 || prefix.length() > 16) {
            return sendErrorMessage(message, "Invalid command prefix given, `%s` is not a valid command prefix, all prefixes must **NOT** contain spaces and be between 1 and 16 characters long.");
        }

        try {
            transformer.getPrefixes().put(category.getName().toLowerCase(), prefix);
            updateGuildPrefixes(message, transformer);
            MessageFactory.makeSuccess(message, "All commands in the `%s` command category now uses the `%s` prefix.",
                    category.getName(), prefix
            ).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean removeCustomPrefix(Message message, GuildTransformer transformer, Category category) {
        transformer.getPrefixes().remove(category.getName().toLowerCase());

        try {
            updateGuildPrefixes(message, transformer);
            MessageFactory.makeSuccess(message, "All commands in the `%s` command category has been reset to use the `%s` prefix.",
                    category.getName(), category.getPrefix()
            ).queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateGuildPrefixes(Message message, GuildTransformer transformer) throws SQLException {
        orion.database.newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", message.getGuild().getId())
                .update(statement -> {
                    statement.set("prefixes", new Gson().toJson(transformer.getPrefixes()));
                });

    }
}
