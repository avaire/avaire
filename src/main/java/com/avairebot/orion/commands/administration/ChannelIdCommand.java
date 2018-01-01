package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChannelIdCommand extends Command {

    public ChannelIdCommand(Orion orion) {
        super(orion, false);
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
    public List<String> getTriggers() {
        return Arrays.asList("channelid", "cid");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        TextChannel channel = message.getTextChannel();
        if (!message.getMentionedChannels().isEmpty()) {
            channel = message.getMentionedChannels().get(0);
        }

        MessageFactory.makeSuccess(message, ":user :id: of the :channel channel is `:targetChannel`")
            .set("targetChannel", channel.getId()).queue();
        return true;
    }
}
