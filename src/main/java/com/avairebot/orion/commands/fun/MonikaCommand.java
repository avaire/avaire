package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandPriority;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MonikaCommand extends Command {

    public MonikaCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Just Monika";
    }

    @Override
    public String getDescription() {
        return "Just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika...";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("justmonika", "monika");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIDDEN;
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        RequestFactory.makeGET("https://i.imgur.com/ZupgGkI.jpg")
            .send((Consumer<Response>) response -> message.getChannel().sendFile(
                response.getResponse().body().byteStream(),
                "just-monika.jpg",
                new MessageBuilder().setEmbed(
                    new EmbedBuilder()
                        .setImage("attachment://just-monika.jpg")
                        .setDescription("Just Monika")
                        .setFooter("Just Monika", null)
                        .build()
                ).build()
            ).queue());

        return true;
    }
}
