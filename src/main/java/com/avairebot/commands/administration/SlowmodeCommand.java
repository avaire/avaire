package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.permissions.Permissions;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.NumberUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SlowmodeCommand extends Command {

    public SlowmodeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Slowmode Command";
    }

    @Override
    public String getDescription() {
        return "Disables the slowmode module or enables it with the given settings, users with the **" + Permissions.MESSAGE_MANAGE.getPermission().getName() + "** permission are exempt from slowmode limits.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <off>` - Disables slowmode for the current channel.",
            "`:command <limit> <decay>` - Enables slowmode with the given settings."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command off` - Disables slowmode",
            "`:command 1 5` - Enables slowmode, allowing one message every five seconds."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(PurgeCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("slowmode");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:guild,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }

        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return endWithFailureToFindTransformer(context);
        }

        ChannelTransformer channelTransformer = guildTransformer.getChannel(context.getChannel().getId());
        if (channelTransformer == null) {
            return endWithFailureToFindTransformer(context);
        }

        if (args.length == 1 && ComparatorUtil.isFuzzyFalse(args[0])) {
            return disableSlowmode(context, guildTransformer, channelTransformer);
        }

        if (args.length == 2 && NumberUtil.isNumeric(args[0]) && NumberUtil.isNumeric(args[1])) {
            return enableSlowmode(context, args, guildTransformer, channelTransformer);
        }

        return sendErrorMessage(context, context.i18n("mustBeValidNumbers"));
    }

    private boolean enableSlowmode(CommandMessage context, String[] args, GuildTransformer guildTransformer, ChannelTransformer channelTransformer) {
        int limit = NumberUtil.getBetween(NumberUtil.parseInt(args[0]), 1, 30);
        int decay = NumberUtil.getBetween(NumberUtil.parseInt(args[1]), 1, 300);

        channelTransformer.getSlowmode().setEnabled(true);
        channelTransformer.getSlowmode().setLimit(limit);
        channelTransformer.getSlowmode().setDecay(decay);

        return updateDatabase(context, guildTransformer, v -> {
            context.makeSuccess(context.i18n("message"))
                .set("limit", limit)
                .set("decay", decay)
                .queue();
        });
    }

    private boolean disableSlowmode(CommandMessage context, GuildTransformer guildTransformer, ChannelTransformer transformer) {
        transformer.getSlowmode().setEnabled(false);

        return updateDatabase(context, guildTransformer, v -> {
            context.makeSuccess(context.i18n("disabled")).queue();
        });
    }

    private boolean endWithFailureToFindTransformer(CommandMessage context) {
        return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "channel settings");
    }

    private boolean updateDatabase(CommandMessage context, GuildTransformer guildTransformer, Consumer<Void> consumer) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            consumer.accept(null);
            return true;
        } catch (SQLException ex) {
            AvaIre.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
        }
        return false;
    }
}
