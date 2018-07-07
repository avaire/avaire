package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChannelIdCommand extends Command {

    public ChannelIdCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Channel ID Command";
    }

    @Override
    public String getDescription() {
        return "Shows the ID of the channel the command was ran in, or the channel tagged in the command.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [channel]`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(ChannelInfoCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("channelid", "cid");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        TextChannel channel = context.getChannel();
        if (!context.getMentionedChannels().isEmpty()) {
            channel = context.getMentionedChannels().get(0);
        }

        context.makeSuccess(context.i18n("message"))
            .set("targetChannel", channel.getId())
            .queue();

        return true;
    }
}
