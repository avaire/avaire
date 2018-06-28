package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NSFWCommand extends Command {

    public NSFWCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "NSFW Command";
    }

    @Override
    public String getDescription() {
        return "Displays the NSFW status of the current channel, additionally on/off can be passed to the command to change the channels NSFW status.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("nsfw");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the NSFW status of the current channel.",
            "`:command <on | off>` - Changes the NSFW status of the current channel.",
            "`:command <channel>` - Displays the mentioned channels NSFW status",
            "`:command <channel> <on | off>` - Changes the NSFW status of the mentioned channel."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command`",
            "`:command on`",
            "`:command #nsfw-stuff`",
            "`:command #general off`"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:all,general.manage_channels",
            "throttle:channel,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendChannelStatus(context, context.getChannel());
        }

        if (args.length == 1) {
            switch (ComparatorUtil.getFuzzyType(args[0])) {
                case TRUE:
                    return updateChannelStatus(context, context.getChannel(), true);

                case FALSE:
                    return updateChannelStatus(context, context.getChannel(), false);

                case UNKNOWN:
                    Channel channel = MentionableUtil.getChannel(context.getMessage(), args);

                    if (channel != null && (channel instanceof TextChannel)) {
                        return sendChannelStatus(context, (TextChannel) channel);
                    }
                    return sendErrorMessage(context, "Invalid channel or status type given!");
            }
        }

        Channel channel = MentionableUtil.getChannel(context.getMessage(), args);
        if (channel == null || !(channel instanceof TextChannel)) {
            return sendErrorMessage(context, "`{0}` is not a valid text channel!", args[0]);
        }

        ComparatorUtil.ComparatorType fuzzyType = ComparatorUtil.getFuzzyType(args[1]);
        if (fuzzyType.equals(ComparatorUtil.ComparatorType.UNKNOWN)) {
            return sendErrorMessage(context, "`{0}` is not a valid status type, you must either pass `on` or `off`.", args[1]);
        }

        return updateChannelStatus(context, (TextChannel) channel, fuzzyType.getValue());
    }

    private boolean updateChannelStatus(CommandMessage context, TextChannel textChannel, boolean status) {
        textChannel.getManager().setNSFW(status).queue(aVoid -> {
            context.makeSuccess("The NSFW status for :textChannel has been changed to **:status**")
                .set("textChannel", textChannel.getAsMention())
                .set("status", status ? "Enabled" : "Disabled")
                .queue();
        }, throwable -> {
            context.makeError("Something went wrong while trying to update the channel status: " + throwable.getLocalizedMessage())
                .queue();
        });

        return true;
    }

    private boolean sendChannelStatus(CommandMessage context, TextChannel textChannel) {
        context.makeInfo("The :textChannel channel currently has NSFW **:status**!")
            .set("textChannel", textChannel.getAsMention())
            .set("status", textChannel.isNSFW() ? "Enabled" : "Disabled")
            .queue();

        return false;
    }
}
