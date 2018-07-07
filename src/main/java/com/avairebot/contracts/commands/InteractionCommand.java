package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.language.I18n;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RandomUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
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

    public InteractionCommand(AvaIre avaire) {
        this(avaire, null, false);
    }

    @Override
    public String getDescription(CommandContext context) {
        return String.format(
            "Sends the **%s** interaction to the mentioned user.",
            getInteraction(context, true)
        );
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
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
        if (user == null) {
            return sendErrorMessage(context, "You must mention a use you want to use the interaction for.");
        }

        context.getChannel().sendTyping().queue();

        List<String> interactionImages = getInteractionImages();
        int imageIndex = RandomUtil.getInteger(interactionImages.size());

        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder
            .setImage("attachment://" + getClass().getSimpleName() + "-" + imageIndex + ".gif")
            .setDescription(buildMessage(context, user))
            .setColor(getInteractionColor());

        messageBuilder.setEmbed(embedBuilder.build());

        try {
            InputStream stream = new URL(interactionImages.get(imageIndex)).openStream();

            context.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + imageIndex + ".gif", messageBuilder.build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String buildMessage(CommandMessage context, User user) {
        if (overwrite) {
            return I18n.format(
                getInteraction(context, false),
                context.getMember().getEffectiveName(),
                context.getGuild().getMember(user).getEffectiveName()
            );
        }

        return String.format("**%s** %s **%s**",
            context.getMember().getEffectiveName(),
            getInteraction(context, false),
            context.getGuild().getMember(user).getEffectiveName()
        );
    }

    private String getInteraction(CommandContext context, boolean isDescription) {
        if (isDescription && overwrite) {
            return getTriggers().get(0);
        }
        return interaction == null ? context.i18nRaw(context.getI18nCommandPrefix()) : interaction;
    }
}
