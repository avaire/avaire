package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.chat.PlaceholderMessage;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RandomUtil;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public abstract class InteractionCommand extends Command {

    private final String interaction;
    private final boolean overwrite;

    public InteractionCommand(Orion orion, String interaction, boolean overwrite) {
        super(orion, false);

        this.overwrite = overwrite;
        this.interaction = interaction;
    }

    public InteractionCommand(Orion orion, String interaction) {
        this(orion, interaction, false);
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
    public String getExampleUsage() {
        return "`:command @Orion`";
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
        if (message.getMentionedUsers().isEmpty()) {
            return sendErrorMessage(message, "You must mention a use you want to use the interaction for.");
        }

        PlaceholderMessage placeholderMessage = MessageFactory.makeEmbeddedMessage(
            message.getChannel(), null, buildMessage(message)
        ).setImage(getInteractionImages().get(
            RandomUtil.getInteger(getInteractionImages().size())
        ));

        if (getInteractionColor() != null) {
            placeholderMessage.setColor(getInteractionColor());
        }

        placeholderMessage.queue();
        return true;
    }

    private String buildMessage(Message message) {
        if (overwrite) {
            return String.format(interaction,
                message.getMember().getEffectiveName(),
                message.getGuild().getMember(message.getMentionedUsers().get(0)).getEffectiveName()
            );
        }

        return String.format("**%s** %s **%s**",
            message.getMember().getEffectiveName(),
            interaction,
            message.getGuild().getMember(message.getMentionedUsers().get(0)).getEffectiveName()
        );
    }
}
