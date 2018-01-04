package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.MessageFactory;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.MentionableUtil;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Arrays;
import java.util.List;

public class ChannelInfoCommand extends Command {

    public ChannelInfoCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getTriggers() {
        return Arrays.asList("channelinfo", "cinfo");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Channel channel = message.getTextChannel();
        if (args.length > 0) {
            channel = MentionableUtil.getChannel(message, args);

            if (channel == null) {
                return sendErrorMessage(message, "I found no channels with the name or ID of `%s`", args[0]);
            }
        }

        Carbon time = Carbon.createFromOffsetDateTime(channel.getCreationTime());

        PlaceholderMessage placeholder = MessageFactory.makeEmbeddedMessage(message.getChannel())
            .setColor(MessageType.INFO.getColor())
            .setTitle("#" + channel.getName())
            .addField("ID", channel.getId(), true)
            .addField("Position", "" + channel.getPosition(), true)
            .addField("Users", "" + channel.getMembers().size(), true)
            .addField("Category", getCategoryFor(channel), true);

        if (channel instanceof TextChannel) {
            TextChannel textChannel = (TextChannel) channel;

            String topic = "*No topic has been set for this channel*";
            if (textChannel.getTopic() != null && textChannel.getTopic().trim().length() > 0) {
                topic = textChannel.getTopic();
            }

            placeholder
                .setDescription(topic)
                .addField("NSFW", textChannel.isNSFW() ? "ON" : "OFF", true);
        }

        if (channel instanceof VoiceChannel) {
            VoiceChannel voiceChannel = (VoiceChannel) channel;
            int bitRate = voiceChannel.getBitrate() / 1000;

            placeholder
                .setDescription("*This voice channel has *")
                .addField("Bit Rate", bitRate + " kbps", true);
        }

        placeholder
            .addField("Created At", time.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(time) + "*", true)
            .queue();

        return true;
    }

    private String getCategoryFor(Channel channel) {
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
