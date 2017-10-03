package com.avairebot.orion.contracts.commands;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandMessage;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public abstract class InteractionCommand extends Command {

    private static final Random random = new Random();
    private final String interaction;

    public InteractionCommand(Orion orion, String interaction) {
        super(orion, false);

        this.interaction = interaction;
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

    public abstract List<String> getInteractionImages();

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (message.getMentionedUsers().isEmpty()) {
            return sendErrorMessage(message, "You must mention a use you want to use the interaction for.");
        }

        int imageIndex = random.nextInt(getInteractionImages().size());

        message.getChannel().sendTyping().queue();
        RequestFactory.makeGET(getInteractionImages().get(imageIndex))
                .send((Consumer<Response>) response -> message.getChannel().sendFile(
                        response.getResponse().body().byteStream(),
                        interaction + "-" + imageIndex + ".gif",
                        new CommandMessage(String.format("**%s** %s **%s**",
                                message.getMember().getEffectiveName(),
                                interaction,
                                message.getGuild().getMember(message.getMentionedUsers().get(0)).getEffectiveName()
                        ))
                ).queue());
        return true;
    }
}
