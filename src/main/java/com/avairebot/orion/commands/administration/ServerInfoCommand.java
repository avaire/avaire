package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.PlaceholderMessage;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.time.Carbon;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerInfoCommand extends Command {

    public ServerInfoCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Server Info Command";
    }

    @Override
    public String getDescription() {
        return "Shows information about the server the command was ran in.";
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
        return Arrays.asList("serverinfo", "sinfo");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Guild guild = message.getGuild();
        Carbon time = Carbon.createFromOffsetDateTime(guild.getCreationTime());

        PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(message.getChannel(), getRoleColor(guild.getSelfMember().getRoles()),
            new MessageEmbed.Field("ID", guild.getId(), true),
            new MessageEmbed.Field("Owner", guild.getOwner().getUser().getName() + "#" + guild.getOwner().getUser().getDiscriminator(), true),
            new MessageEmbed.Field("Text Channels", "" + guild.getTextChannels().size(), true),
            new MessageEmbed.Field("Voice Channels", "" + guild.getVoiceChannels().size(), true),
            new MessageEmbed.Field("Members", "" + guild.getMembers().size(), true),
            new MessageEmbed.Field("Roles", "" + guild.getRoles().size(), true),
            new MessageEmbed.Field("Region", guild.getRegion().getName(), true),
            new MessageEmbed.Field("Created At", time.format("EEE, dd MMM yyyy HH:mm") + "\n*About " + shortenDiffForHumans(time) + "*", true)
        ).setTitle(guild.getName()).setThumbnail(guild.getIconUrl());

        if (!guild.getEmotes().isEmpty()) {
            boolean hasMany = guild.getEmotes().size() > 17;
            placeholderMessage.addField(new MessageEmbed.Field(
                String.format("Custom Emojis (%s)", guild.getEmotes().size()),
                guild.getEmotes().stream()
                    .map(emote -> emote.getName() + (hasMany ? "" : " " + emote.getAsMention()))
                    .collect(Collectors.joining(", ")),
                true
            ));
        }

        placeholderMessage.queue();
        return true;
    }

    private String shortenDiffForHumans(Carbon carbon) {
        String diff = carbon.diffForHumans();
        if (!diff.contains("and")) {
            return diff;
        }
        return diff.split("and")[0] + "ago";
    }

    private Color getRoleColor(List<Role> roles) {
        for (Role role : roles) {
            if (role.getColor() != null) return role.getColor();
        }
        return Color.decode("#E91E63");
    }
}
