package com.avairebot.orion.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.commands.CommandContainer;
import com.avairebot.orion.commands.CommandHandler;
import com.avairebot.orion.commands.utility.RankCommand;
import com.avairebot.orion.contracts.ai.Intent;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class RequestLevel extends Intent {

    public RequestLevel(Orion orion) {
        super(orion);
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

        GuildTransformer guildTransformer = GuildController.fetchGuild(orion, message);
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            MessageFactory.makeWarning(message,
                "This server doesn't have the `Levels & Experience` feature enabled so I can't tell you what level you are :("
            ).queue();
            return;
        }

        container.getCommand().onCommand(message, new String[]{"---skip-mentions"});
    }
}
