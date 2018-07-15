package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VoteOptCommand extends Command {

    public VoteOptCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Vote Opt Command";
    }

    @Override
    public String getDescription() {
        return "Opt in or out of getting vote messages when you vote for Ava.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <in|out>` - Opt in/out of vote messages when you vote");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command in` - Enables vote messages when you vote.",
            "`:command out` - Disables vote messages when you vote."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("voteopt");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "isDMMessage", "throttle:user,1,10"
        );
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIDDEN;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "Missing argument, you must either parse `in` or `out` to opt in or out of vote messages.");
        }

        int opt = getOptValue(args[0]);
        if (opt == -1) {
            return sendErrorMessage(context, "Invalid argument, you must either parse `in` or `out` to opt in or out of vote messages.");
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.VOTES_TABLE_NAME)
                .useAsync(true)
                .where("user_id", context.getAuthor().getIdLong())
                .update(statement -> statement.set("opt_in", opt));
        } catch (SQLException ignored) {
        }

        context.makeSuccess("You have now opt-:type of getting vote messages.")
            .set("type", opt == 0 ? "out" : "in")
            .queue();

        avaire.getVoteManager().getVoteEntityWithFallback(avaire, context.getAuthor()).setOptIn(opt == 1);

        return true;
    }

    private int getOptValue(String arg) {
        if (arg.equalsIgnoreCase("in")) return 1;
        if (arg.equalsIgnoreCase("out")) return 0;
        return -1;
    }
}
