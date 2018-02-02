package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class RequestLevel extends Intent {

    public RequestLevel(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.level";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        CommandContainer container = CommandHandler.getCommand(RankCommand.class);
        if (container == null) {
            return;
        }

        GuildTransformer guildTransformer = GuildController.fetchGuild(avaire, message);
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            MessageFactory.makeWarning(message,
                "This server doesn't have the `Levels & Experience` feature enabled so I can't tell you what level you are :("
            ).queue();
            return;
        }

        container.getCommand().onCommand(new CommandMessage(message), new String[]{"---skip-mentions"});
    }
}
