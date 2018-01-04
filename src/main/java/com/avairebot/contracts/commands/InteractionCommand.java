package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandPriority;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RandomUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public abstract class InteractionCommand extends Command {

    private final String interaction;
    private final boolean overwrite;

    public InteractionCommand(AvaIre avaire, String interaction, boolean overwrite) {
        super(avaire, false);

        this.overwrite = overwrite;
        this.interaction = interaction;
    }

    public InteractionCommand(AvaIre avaire, String interaction) {
        this(avaire, interaction, false);
    }

    @Override
    public String getDescription() {
        return String.format("Sends the **%s** interaction to the mentioned user.", interaction);
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user>`");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @AvaIre`");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIGH;
    }

    public Color getInteractionColor() {
        return null;
    }

    public abstract List<String> getInteractionImages();

    @Override
    public boolean onCommand(Message message, String[] args) {
        User user = MentionableUtil.getUser(message, args, 1);
        if (user == null) {
            return sendErrorMessage(message, "You must mention a use you want to use the interaction for.");
        }

        message.getChannel().sendTyping().queue();

        int imageIndex = RandomUtil.getInteger(getInteractionImages().size());

        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder
            .setImage("attachment://" + getClass().getSimpleName() + "-" + imageIndex + ".gif")
            .setDescription(buildMessage(message, user))
            .setColor(getInteractionColor());

        messageBuilder.setEmbed(embedBuilder.build());

        try {
            InputStream stream = new URL(getInteractionImages().get(imageIndex)).openStream();

            message.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + imageIndex + ".gif", messageBuilder.build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String buildMessage(Message message, User user) {
        if (overwrite) {
            return String.format(interaction,
                message.getMember().getEffectiveName(),
                message.getGuild().getMember(user).getEffectiveName()
            );
        }

        return String.format("**%s** %s **%s**",
            message.getMember().getEffectiveName(),
            interaction,
            message.getGuild().getMember(message.getMentionedUsers().get(0)).getEffectiveName()
        );
    }
}
