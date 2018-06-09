package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModlogHistoryCommand extends Command {

    public ModlogHistoryCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Modlog History Command";
    }

    @Override
    public String getDescription() {
        return "Displays the modlog history for the mentioned user, this will display all past warnings, bans, soft bans, kicks, and voice kicks.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user>` - Displays the modlog history for the mentioned user."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command @Senither` - Displays all the bad things Senither has done."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ModlogCommand.class,
            ModlogReasonCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("modloghistory", "history");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, "You must mention a valid `user` you want to display the modlog history for.");
        }

        try {
            Collection items = avaire.getDatabase().newQueryBuilder(Constants.LOG_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .where("target_id", user.getId())
                .get();

            if (items.isEmpty()) {
                context.makeWarning("No modlog history was found for this user.")
                    .setTitle(user.getName() + "#" + user.getDiscriminator() + " History (0)")
                    .queue();

                return true;
            }

            List<String> records = new ArrayList<>();
            items.forEach(row -> {
                ModlogType type = ModlogType.fromId(row.getInt("type", 0));
                String reason = row.getString("reason", "No reason was given.");

                records.add(String.format("**Case ID #%s** | %s\n\t%s",
                    row.getString("modlogCase"),
                    type == null ? "Unknown" : type.getName(),
                    reason == null ? "No reason was given." : reason
                ));
            });

            List<String> messages = new ArrayList<>();
            SimplePaginator paginator = new SimplePaginator(records, 5);
            if (args.length > 1) {
                paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
            }

            paginator.forEach((_index, _key, val) -> messages.add((String) val));
            messages.add("\n" + paginator.generateFooter(generateCommandTrigger(context.getMessage())));

            context.makeInfo(String.join("\n", messages))
                .setTitle(user.getName() + "#" + user.getDiscriminator() + " History (" + paginator.getTotal() + ")")
                .queue();
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to load log records for user {} in guild {}",
                user.getId(), context.getGuild().getId(), e
            );

            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "modlog records");
        }

        return true;
    }
}
