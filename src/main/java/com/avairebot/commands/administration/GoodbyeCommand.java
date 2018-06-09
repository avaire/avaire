package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.ComparatorUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@CacheFingerprint(name = "welcome-goodbye-command")
public class GoodbyeCommand extends Command {

    public GoodbyeCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Goodbye Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the goodbye messages on or off for the current channel.";
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            GoodbyeMessageCommand.class,
            WelcomeCommand.class,
            WelcomeMessageCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("goodbye", "bye");
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
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer channelTransformer = guildTransformer.getChannel(context.getChannel().getId());

        if (channelTransformer == null) {
            return sendErrorMessage(context,
                "Something went wrong while trying to get the channel transformer object, please contact one of my developers to look into this issue."
            );
        }

        ComparatorUtil.ComparatorType type = args.length == 0 ?
            ComparatorUtil.ComparatorType.UNKNOWN :
            ComparatorUtil.getFuzzyType(args[0]);

        switch (type) {
            case TRUE:
            case FALSE:
                channelTransformer.getGoodbye().setEnabled(type.getValue());
                break;

            case UNKNOWN:
                channelTransformer.getGoodbye().setEnabled(!channelTransformer.getGoodbye().isEnabled());
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            String note = "";
            if (channelTransformer.getGoodbye().isEnabled()) {
                note = String.format("\nYou can customize the message by using `%sgoodbyemessage [message]`",
                    generateCommandPrefix(context.getMessage())
                );
            }

            context.makeSuccess("The `Goodbye` module has been **:status** for the :channel channel." + note)
                .set("status", channelTransformer.getGoodbye().isEnabled() ? "Enabled" : "Disabled")
                .queue();
        } catch (SQLException ex) {
            AvaIre.getLogger().error("Failed to update the goodbye status", ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
