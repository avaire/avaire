package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.MessageType;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.time.Carbon;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.OffsetDateTime;
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

        OffsetDateTime time = channel.getCreationTime();
        Carbon carbon = Carbon.create(
            time.getYear(),
            time.getMonthValue(),
            time.getDayOfMonth(),
            time.getHour(),
            time.getMinute(),
            time.getSecond()
        );

        EmbedBuilder builder = MessageFactory.makeEmbeddedMessage(MessageType.INFO.getColor(),
            new MessageEmbed.Field("ID", channel.getId(), true),
            new MessageEmbed.Field("Position", "" + channel.getPosition(), true),
            new MessageEmbed.Field("Users", "" + channel.getMembers().size(), true),
            new MessageEmbed.Field("Category", getCategoryFor(channel), true),
            new MessageEmbed.Field("NSFW", channel.isNSFW() ? "ON" : "OFF", true),
            new MessageEmbed.Field("Created At", carbon.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(carbon) + "*", true)
        ).setTitle("#" + channel.getName()).setDescription("*No topic has been set for this channel*");

        if (channel.getTopic() != null) {
            builder.setDescription(channel.getTopic());
        }

        message.getChannel().sendMessage(builder.build()).queue();
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
