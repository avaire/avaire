package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.blacklist.Scope;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.utilities.NumberUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlacklistCommand extends SystemCommand {

    public BlacklistCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Blacklist Command";
    }

    @Override
    public String getDescription() {
        return "Add, Remove, and list users and servers on the blacklist.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list` - Lists users and servers on the blacklist",
            "`:command remove <id>` - Removes the entry with the given ID from the blacklist",
            "`:command add <type> <id> <reason>` - Add the type with the given ID to the blacklist"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("blacklist");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing parameter, a valid `action` must be given!");
        }

        switch (args[0].toLowerCase()) {
            case "list":
                return listBlacklist(context, Arrays.copyOfRange(args, 1, args.length));

            case "add":
                return addEntryToBlacklist(context, Arrays.copyOfRange(args, 1, args.length));

            case "remove":
                return removeEntryFromBlacklist(context, Arrays.copyOfRange(args, 1, args.length));

            default:
                return sendErrorMessage(context, "Invalid `action` given, a valid `action` must be given!");
        }
    }

    private boolean listBlacklist(CommandMessage context, String[] args) {
        try {
            List<String> records = new ArrayList<>();

            avaire.getDatabase().newQueryBuilder(Constants.BLACKLIST_TABLE_NAME).get().forEach(dataRow -> {
                Scope type = Scope.parse(dataRow.getString("type"));
                if (type == null) {
                    AvaIre.getLogger().warn("BLACKLIST - A record was found with an invalid scope! " + dataRow.getString("id"));
                    return;
                }

                records.add(String.format("`%s` %s\n\t_\"%s\"_",
                    type.getPrefix(),
                    dataRow.getString("id"),
                    dataRow.getString("reason", "No reason given")
                ));
            });

            SimplePaginator paginator = new SimplePaginator(records, 10, 1);
            if (args.length > 0) {
                paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
            }

            List<String> messages = new ArrayList<>();
            paginator.forEach((index, key, val) -> messages.add((String) val));

            context.makeInfo(String.join("\n", messages) + "\n\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage()) + " list"))
                .setTitle("Blacklist Page #" + paginator.getCurrentPage())
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean removeEntryFromBlacklist(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing arguments, the `id` argument is required!");
        }

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return sendErrorMessage(context, "Invalid ID given, the ID must be a valid number value!");
        }

        if (!avaire.getBlacklist().isBlacklisted(id)) {
            return sendErrorMessage(context, "There are no records in the blacklist with an ID of `{0}`", "" + id);
        }

        avaire.getBlacklist().remove(id);

        context.makeSuccess("The Blacklist record with an ID of **:id** has been removed from the blacklist")
            .set("id", id)
            .queue();

        return true;
    }

    private boolean addEntryToBlacklist(CommandMessage context, String[] args) {
        if (args.length < 2) {
            return sendErrorMessage(context, "Missing arguments, `type` and `id` argument required!");
        }

        Scope scope = Scope.parse(args[0]);
        if (scope == null) {
            return sendErrorMessage(context, "Invalid type given, the type must be a valid blacklist scope!");
        }

        long id;
        try {
            id = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            return sendErrorMessage(context, "Invalid ID given, the ID must be a valid number value!");
        }

        String reason = null;
        args = Arrays.copyOfRange(args, 2, args.length);
        if (args.length > 0) {
            reason = String.join(" ", args);
        }

        avaire.getBlacklist().addIdToBlacklist(scope, id, reason);

        context.makeSuccess("The **:type** with an ID of **:id** has been added to the blacklist!")
            .set("type", scope.name().toLowerCase())
            .set("id", id)
            .queue();

        return true;
    }
}
