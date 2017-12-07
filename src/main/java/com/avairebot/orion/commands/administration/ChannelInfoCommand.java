package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.time.Carbon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public class ChannelInfoCommand extends Command {

    public ChannelInfoCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Channel Info Command";
    }

    @Override
    public String getDescription() {
        return "Shows information about the channel the command was run in, or the mentioned channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("channelinfo", "cinfo");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        TextChannel channel = message.getTextChannel();
        if (message.getMentionedChannels().isEmpty()) {
            channel = message.getTextChannel();
        }

        Carbon time = Carbon.createFromOffsetDateTime(channel.getCreationTime());

        String topic = "*No topic has been set for this channel*";
        if (channel.getTopic() != null) {
            topic = channel.getTopic();
        }

        MessageFactory.makeInfo(message, topic)
            .setTitle("#" + channel.getName())
            .addField("ID", channel.getId(), true)
            .addField("Position", "" + channel.getPosition(), true)
            .addField("Users", "" + channel.getMembers().size(), true)
            .addField("Category", getCategoryFor(channel), true)
            .addField("NSFW", channel.isNSFW() ? "ON" : "OFF", true)
            .addField("Created At", time.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(time) + "*", true)
            .queue();

        return true;
    }

    private String getCategoryFor(TextChannel channel) {
        if (channel.getParent() == null) {
            return "*No Category*";
        }
        return channel.getParent().getName();
    }

    private String shortenDiffForHumans(Carbon carbon) {
        String diff = carbon.diffForHumans();
        if (!diff.contains("and")) {
            return diff;
        }
        return diff.split("and")[0] + "ago";
    }
}
