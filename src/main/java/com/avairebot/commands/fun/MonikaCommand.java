package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.Command;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MonikaCommand extends Command {

    public MonikaCommand(AvaIre avaire) {
        super(avaire);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("https://i.imgur.com/ZupgGkI.jpg")
            .send((Consumer<Response>) response -> {
                ResponseBody body = response.getResponse().body();

                if (body == null) {
                    return;
                }

                context.getChannel().sendFile(body.byteStream(),
                    "just-monika.jpg",
                    new MessageBuilder().setEmbed(
                        new EmbedBuilder()
                            .setImage("attachment://just-monika.jpg")
                            .setDescription("Just Monika")
                            .setFooter("Just Monika", null)
                            .build()
                    ).build()
                ).queue();
            });

        return true;
    }
}
